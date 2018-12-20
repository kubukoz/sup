package sup

import cats.implicits._
import cats.effect.implicits._

import cats.effect.{Concurrent, Timer}
import cats.~>

import scala.concurrent.duration.FiniteDuration

trait HealthCheck[F[_]] {
  def check: F[Health]
  def mapK(f: F ~> F): HealthCheck[F] = new MappedHealthCheck[F](this, f)
}

object HealthCheck {

  def timed[F[_]: Concurrent: Timer](underlying: HealthCheck[F], time: FiniteDuration): HealthCheck[F] =
    new TimedHealthCheck(underlying, time)
}

final class TimedHealthCheck[F[_]: Concurrent: Timer](underlying: HealthCheck[F], time: FiniteDuration)
    extends HealthCheck[F] {

  override val check: F[Health] = underlying.check.timeoutTo(time, Health.bad.pure[F])
}

final class MappedHealthCheck[F[_]](underlying: HealthCheck[F], function: F ~> F) extends HealthCheck[F] {
  override val check: F[Health] = function(underlying.check)
}
