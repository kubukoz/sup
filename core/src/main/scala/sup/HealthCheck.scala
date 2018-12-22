package sup

import cats.{~>, Applicative, Eq, Functor, Monoid}
import cats.implicits._
import cats.tagless.FunctorK
import sup.transformed.{LeftMappedHealthCheck, MappedKHealthCheck, MappedResultHealthCheck, TransformedHealthCheck}

/**
  * A health check.
  * F is the effect of making a healthcheck (e.g. IO for calls to external systems).
  *
  * H is the container of results. See [[HealthResult]] for examples.
  * */
abstract class HealthCheck[F[_], H[_]] {
  def check: F[HealthResult[H]]

  def leftMapK[G[_]](fg: F ~> G): HealthCheck[G, H] =
    new LeftMappedHealthCheck[F, G, H](this, fg)

  def transform[G[_], I[_]](f: F[HealthResult[H]] => G[HealthResult[I]]): HealthCheck[G, I] =
    new TransformedHealthCheck[F, G, H, I](this, f)

  def mapK[I[_]](f: H ~> I)(implicit F: Functor[F]): HealthCheck[F, I] = new MappedKHealthCheck(this, f)

  def mapResult[I[_]](f: HealthResult[H] => HealthResult[I])(implicit F: Functor[F]): HealthCheck[F, I] =
    new MappedResultHealthCheck(this, f)
}

object HealthCheck {

  /**
    * A healthcheck that always returns the supplied health value.
    * */
  def const[F[_]: Applicative, H[_]: Applicative](health: Health): HealthCheck[F, H] = new HealthCheck[F, H] {
    override val check: F[HealthResult[H]] = HealthResult.const[H](health).pure[F]
  }

  def liftF[F[_], H[_]](_check: F[HealthResult[H]]): HealthCheck[F, H] = new HealthCheck[F, H] {
    override val check: F[HealthResult[H]] = _check
  }

  implicit def functorK[F[_]: Functor]: FunctorK[HealthCheck[F, ?[_]]] = new FunctorK[HealthCheck[F, ?[_]]] {
    override def mapK[G[_], H[_]](fgh: HealthCheck[F, G])(gh: G ~> H): HealthCheck[F, H] = fgh.mapK(gh)
  }

  implicit def checkMonoid[F[_]: Applicative, H[_]: Applicative](
    implicit M: Monoid[Health]): Monoid[HealthCheck[F, H]] =
    new Monoid[HealthCheck[F, H]] {
      override val empty: HealthCheck[F, H] = HealthCheck.const[F, H](M.empty)
      override def combine(x: HealthCheck[F, H], y: HealthCheck[F, H]): HealthCheck[F, H] = new HealthCheck[F, H] {
        override val check: F[HealthResult[H]] = Applicative.monoid[F, HealthResult[H]].combine(x.check, y.check)
      }
    }

  implicit def healthCheckEq[F[_], H[_]](implicit F: Eq[F[HealthResult[H]]]): Eq[HealthCheck[F, H]] = Eq.by(_.check)
}
