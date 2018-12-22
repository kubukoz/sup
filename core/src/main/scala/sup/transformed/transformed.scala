package sup.transformed

import cats.implicits._
import cats.{~>, Functor}
import sup.{HealthCheck, HealthResult}
import cats.tagless.implicits._

private[sup] final class LeftMappedHealthCheck[F[_], G[_], H[_]](underlying: HealthCheck[F, H], fg: F ~> G)
    extends HealthCheck[G, H] {

  val check: G[HealthResult[H]]                               = fg(underlying.check)
  override def leftMapK[I[_]](fg2: G ~> I): HealthCheck[I, H] = new LeftMappedHealthCheck(underlying, fg2 compose fg)
}

private[sup] final class TransformedHealthCheck[F[_], G[_], H[_], I[_]](underlying: HealthCheck[F, H],
                                                                        f: F[HealthResult[H]] => G[HealthResult[I]])
    extends HealthCheck[G, I] {

  val check: G[HealthResult[I]] = f(underlying.check)
  override def transform[J[_], K[_]](f2: G[HealthResult[I]] => J[HealthResult[K]]): HealthCheck[J, K] =
    new TransformedHealthCheck(underlying, f2 compose f)
}

private[sup] final class MappedKHealthCheck[F[_]: Functor, G[_], H[_]](underlying: HealthCheck[F, G], f: G ~> H)
    extends HealthCheck[F, H] {
  override val check: F[HealthResult[H]] = underlying.check.map(_.mapK(f))
}
