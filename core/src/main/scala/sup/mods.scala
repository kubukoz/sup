package sup

import cats.{~>, Applicative, Id}
import cats.effect.{Concurrent, Timer}

import scala.concurrent.duration.FiniteDuration
import cats.effect.implicits._
import cats.implicits._
import sup.data.Tagged

object mods {

  /**
    * Fail the health check with [[Health.Sick]] in case the check takes longer than `duration`.
    *
    * Use with [[HealthCheck.transform]].
    * */
  def timeoutToSick[F[_]: Concurrent: Timer, H[_]: Applicative](
    duration: FiniteDuration): F[HealthResult[H]] => F[HealthResult[H]] = {
    timeoutToDefault(Health.Sick, duration)
  }

  /**
    * Fallback to the provided value in case the check takes longer than `duration`.
    *
    * Use with [[HealthCheck.transform]].
    * */
  def timeoutToDefault[F[_]: Concurrent: Timer, H[_]: Applicative](
    default: Health,
    duration: FiniteDuration): F[HealthResult[H]] => F[HealthResult[H]] = {
    _.timeoutTo(duration, HealthResult(default.pure[H]).pure[F])

  }

  /**
    * Fail the health check with a failure (as defined by [[Concurrent.timeout]] for F)
    * in case the check takes longer than `duration`.
    *
    * Use with [[HealthCheck.leftMapK]].
    * */
  def timeoutToFailure[F[_]: Concurrent: Timer, H[_]](duration: FiniteDuration): F ~> F = λ[F ~> F] {
    _.timeout(duration)
  }

  /**
    * Tag a health check with a value.
    *
    * Use with [[HealthCheck.mapK]].
    * */
  def tagWith[Tag](tag: Tag): Id ~> Tagged[Tag, ?] = λ[Id ~> Tagged[Tag, ?]](Tagged(tag, _))

  /**
    * Unwrap a tagged health check (dual of `tagWith`).
    *
    * Use with [[HealthCheck.mapK]].
    * */
  def untag[Tag]: Tagged[Tag, ?] ~> Id = λ[Tagged[Tag, ?] ~> Id](_.health)
}
