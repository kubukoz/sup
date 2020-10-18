package sup

import cats.data.{EitherK, Tuple2K}
import cats.{Applicative, ApplicativeError, FlatMap, Functor, Id, Semigroup}

import scala.concurrent.duration.FiniteDuration
import cats.effect.kernel.implicits._
import cats.implicits._
import sup.data.Tagged
import cats.effect.kernel.GenTemporal
import cats.effect.kernel.Temporal

object mods {

  /** Fail the health check with [[Health.Sick]] in case the check takes longer than `duration`.
    */
  def timeoutToSick[F[_]: GenTemporal[*[_], E], E, H[_]: Applicative](
    duration: FiniteDuration
  ): HealthCheckEndoMod[F, H] =
    timeoutToDefault(Health.Sick, duration)

  /** Fallback to the provided value in case the check takes longer than `duration`.
    */
  def timeoutToDefault[F[_]: GenTemporal[*[_], E], E, H[_]: Applicative](
    default: Health,
    duration: FiniteDuration
  ): HealthCheckEndoMod[F, H] =
    _.transform {
      _.timeoutTo(duration, HealthResult(default.pure[H]).pure[F])
    }

  /** Fail the health check with a failure (as defined by [[Concurrent.timeout]] for F)
    * in case the check takes longer than `duration`.
    */
  def timeoutToFailure[F[_]: Temporal, H[_]](duration: FiniteDuration): HealthCheckEndoMod[F, H] =
    _.transform {
      _.timeout(duration)
    }

  /** Recover any errors that might happen in F (to a Sick result)
    */
  def recoverToSick[F[_], H[_]: Applicative, E](implicit F: ApplicativeError[F, E]): HealthCheckEndoMod[F, H] =
    _.transform {
      _.orElse(HealthResult.const[H](Health.Sick).pure[F])
    }

  /** Tag a health check with a value.
    */
  def tagWith[F[_]: Functor, Tag](tag: Tag): HealthCheckMod[F, Id, F, Tagged[Tag, *]] =
    _.mapResult(_.transform(Tagged(tag, _)))

  /** Unwrap a tagged health check (dual of `tagWith`).
    */
  def untag[F[_]: Functor, Tag]: HealthCheckMod[F, Tagged[Tag, *], F, Id] =
    _.mapResult(_.transform[Id](_.health))

  /** Combines containers in a Tuple2K using the given semigroup. Useful in conjunction with HealthCheck.{`tupled`, `parTupled`}.
    */
  def combineTuple2K[F[_]: Functor, H[_]](implicit S: Semigroup[H[Health]]): HealthCheckMod[F, Tuple2K[H, H, *], F, H] =
    _.mapResult {
      _.transform(tuple => tuple.first |+| tuple.second)
    }

  /** Merges an EitherK of the same container type. Useful in conjunction with HealthCheck.{`either`, `race`}.
    */
  def mergeEitherK[F[_]: Functor, H[_]]: HealthCheckMod[F, EitherK[H, H, *], F, H] = _.mapResult {
    _.transform(_.run.merge)
  }

  /** Runs the `before` action before the healthcheck, and the `after` action once it has a result.
    */
  def surround[F[_]: FlatMap, H[_]](before: F[Unit])(after: HealthResult[H] => F[Unit]): HealthCheckEndoMod[F, H] =
    _.transform {
      before *> _.flatTap(after)
    }
}
