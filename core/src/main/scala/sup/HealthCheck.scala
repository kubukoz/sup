package sup

import cats.{~>, Functor}
import sup.algebra.FunctorK
import sup.transformed.{LeftMappedHealthCheck, MappedKHealthCheck, TransformedHealthCheck}

trait HealthCheck[F[_], H[_]] {
  def check: F[HealthResult[H]]

  def leftMapK[G[_]](fg: F ~> G): HealthCheck[G, H] =
    new LeftMappedHealthCheck[F, G, H](this, fg)

  def transform[G[_], I[_]](f: F[HealthResult[H]] => G[HealthResult[I]]): HealthCheck[G, I] =
    new TransformedHealthCheck[F, G, H, I](this, f)
}

object HealthCheck {
  implicit def functorK[F[_]: Functor]: FunctorK[HealthCheck[F, ?[_]]] = new FunctorK[HealthCheck[F, ?[_]]] {
    override def mapK[G[_], H[_]](fgh: HealthCheck[F, G])(gh: G ~> H): HealthCheck[F, H] =
      new MappedKHealthCheck(fgh, gh)
  }
}
