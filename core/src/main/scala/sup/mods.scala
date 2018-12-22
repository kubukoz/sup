package sup
import cats.Applicative
import cats.effect.{Concurrent, Timer}

import scala.concurrent.duration.FiniteDuration
import cats.effect.implicits._
import cats.implicits._

object mods {
  /**
    * Use with [[HealthCheck.leftMapK]].
    * */
  def timed[F[_]: Concurrent: Timer, H[_]: Applicative](
    duration: FiniteDuration): F[HealthResult[H]] => F[HealthResult[H]] = {
    _.timeoutTo(duration, HealthResult(Health.sick.pure[H]).pure[F])
  }
}
