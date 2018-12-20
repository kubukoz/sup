package sup

import scala.concurrent.duration.FiniteDuration
import cats.effect.{Concurrent, Timer}
import cats.effect.implicits._
import cats.~>

trait HealthCheck[F[_]] {
  def check: F[Health]
  def mapK(f: F ~> F): HealthCheck[F] = new MappedHealthCheck[F](this, f)
}

object HealthCheck {

  def timed[F[_]: Concurrent: Timer](underlying: HealthCheck[F], time: FiniteDuration): HealthCheck[F] =
    underlying.mapK(λ[F ~> F](_.timeout(time)))
}

final class MappedHealthCheck[F[_]](underlying: HealthCheck[F], function: F ~> F) extends HealthCheck[F] {
  override val check: F[Health] = function(underlying.check)
}
