package sup

import cats.implicits._
import cats.effect.implicits._

import cats.effect.{Concurrent, Timer}

import scala.concurrent.duration.FiniteDuration
import cats.Group
import cats.Monoid
import cats.Functor

trait HealthCheck[F[_]] {
  def check: F[Health]
  def modifyF(f: F[Health] => F[Health]): HealthCheck[F] = modify(new HealthCheckMod[F](f))
  def modify(mod: HealthCheckMod[F]): HealthCheck[F] = new ModifiedHealthCheck[F](this, mod)
}

final class HealthCheckMod[F[_]](val modify: F[Health] => F[Health]) extends AnyVal

object HealthCheckMod extends HealthCheckModInstances0 {

  def timed[F[_]: Concurrent: Timer](time: FiniteDuration): HealthCheckMod[F] =
    new HealthCheckMod[F](_.timeoutTo(time, Health.bad.pure[F]))
}

private[sup] trait HealthCheckModInstances0 extends HealthCheckModInstances1 {

  //todo lawless?
//  implicit def group[F[_]: Functor]: Group[HealthCheckMod[F]] = new HealthCheckModMonoid[F] with Group[HealthCheckMod[F]] {
//    def inverse(mod: HealthCheckMod[F]): HealthCheckMod[F] = new HealthCheckMod[F](mod.modify(_).map(Health.inverse))
//  }
}

private[sup] trait HealthCheckModInstances1 {
  implicit def monoid[F[_]]: Monoid[HealthCheckMod[F]] = new HealthCheckModMonoid[F]
}

private[sup] class HealthCheckModMonoid[F[_]] extends Monoid[HealthCheckMod[F]] {
  def empty: HealthCheckMod[F] = new HealthCheckMod[F](identity)

  def combine(a: HealthCheckMod[F], b: HealthCheckMod[F]): HealthCheckMod[F] =
    new HealthCheckMod[F](a.modify compose b.modify)
}

final class ModifiedHealthCheck[F[_]](underlying: HealthCheck[F], modifier: HealthCheckMod[F]) extends HealthCheck[F] {
  override val check: F[Health] = modifier.modify(underlying.check)
  override def modify(mod: HealthCheckMod[F]): HealthCheck[F] = new ModifiedHealthCheck[F](underlying, mod |+| modifier)
}
