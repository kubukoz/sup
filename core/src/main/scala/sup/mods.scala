package sup

import cats.data.{EitherK, Tuple2K}
import cats.{~>, Applicative, Functor, Id, Semigroup}
import cats.effect.{Concurrent, Timer}

import scala.concurrent.duration.FiniteDuration
import cats.effect.implicits._
import cats.implicits._
import sup.data.Tagged

object mods {

  /**
    * Fail the health check with [[Health.Sick]] in case the check takes longer than `duration`.
    * */
  def timeoutToSick[F[_]: Concurrent: Timer, H[_]: Applicative](duration: FiniteDuration): HealthCheckMod[F, H, F, H] =
    timeoutToDefault(Health.Sick, duration)

  /**
    * Fallback to the provided value in case the check takes longer than `duration`.
    * */
  def timeoutToDefault[F[_]: Concurrent: Timer, H[_]: Applicative](
    default: Health,
    duration: FiniteDuration): HealthCheckMod[F, H, F, H] =
    _.transform {
      _.timeoutTo(duration, HealthResult(default.pure[H]).pure[F])
    }

  /**
    * Fail the health check with a failure (as defined by [[Concurrent.timeout]] for F)
    * in case the check takes longer than `duration`.
    * */
  def timeoutToFailure[F[_]: Concurrent: Timer, H[_]](duration: FiniteDuration): HealthCheckMod[F, H, F, H] =
    _.transform {
      _.timeout(duration)
    }

  /**
    * Tag a health check with a value.
    * */
  def tagWith[F[_]: Functor, Tag](tag: Tag): HealthCheckMod[F, Id, F, Tagged[Tag, ?]] =
    _.mapResult(_.transform(Tagged(tag, _)))

  /**
    * Unwrap a tagged health check (dual of `tagWith`).
    *
    * Use with [[HealthCheck.mapK]].
    * */
  def untag[F[_]: Functor, Tag]: HealthCheckMod[F, Tagged[Tag, ?], F, Id] =
    _.mapResult(_.transform[Id](_.health))

  /**
    * Combines containers in a Tuple2K using the given semigroup. Useful in conjunction with HealthCheck.{`tupled`, `parTupled`}.
    * */
  def combineTuple2K[F[_]: Functor, H[_]](implicit S: Semigroup[H[Health]]): HealthCheckMod[F, Tuple2K[H, H, ?], F, H] =
    _.mapResult {
      _.transform(tuple => tuple.first |+| tuple.second)
    }

  /**
    * Merges an EitherK of the same container type. Useful in conjunction with HealthCheck.{`either`, `race`}.
    *
    * Use with [[HealthCheck.mapResult]] and `HealthResult.mapK`.
    * */
  def mergeEitherK[F[_]: Functor, H[_]]: HealthCheckMod[F, EitherK[H, H, ?], F, H] = _.mapResult {
    _.transform(_.run.merge)
  }
}
