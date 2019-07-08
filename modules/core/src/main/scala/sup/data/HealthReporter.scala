package sup.data

import cats.data.NonEmptyList
import cats.implicits._
import cats.kernel.Semigroup
import cats.{Apply, Functor, NonEmptyTraverse, Parallel, Reducible}
import cats.temp.par._
import sup._

object HealthReporter {

  /**
    * Equivalent to [[wrapChecks]] for G = NonEmptyList, with more pleasant syntax.
    * */
  def fromChecks[F[_]: Apply, H[_]: Reducible](
    first: HealthCheck[F, H],
    rest: HealthCheck[F, H]*
  ): HealthReporter[F, NonEmptyList, H] =
    wrapChecks(NonEmptyList(first, rest.toList))

  /**
    * Constructs a healthcheck from a non-empty structure G of healthchecks.
    * The status of the whole check is determined using the results of given checks and the semigroup of Health.
    *
    * e.g. if all checks need to return Healthy for the whole thing to be healthy, use [[Health.allHealthyCommutativeMonoid]].
    */
  def wrapChecks[F[_]: Apply, G[_]: NonEmptyTraverse, H[_]: Reducible](
    checks: G[HealthCheck[F, H]]
  ): HealthReporter[F, G, H] = HealthCheck.liftF {

    checks.nonEmptyTraverse(_.check).map(fromResults[G, H])
  }

  /**
    * Constructs a healthcheck from a non-empty structure G of healthchecks that are run in parallel.
    * The status of the whole check is determined using the results of given checks and the semigroup of Health.
    *
    * e.g. if all checks need to return Healthy for the whole thing to be healthy, use [[Health.allHealthyCommutativeMonoid]].
    */
  def parWrapChecks[F[_]: NonEmptyPar: Functor, G[_]: NonEmptyTraverse, H[_]: Reducible](
    checks: G[HealthCheck[F, H]]
  ): HealthReporter[F, G, H] = HealthCheck.liftF {

    Parallel.parNonEmptyTraverse(checks)(_.check).map(fromResults[G, H])
  }

  def fromResults[G[_]: Reducible: Functor, H[_]: Reducible](
    results: G[HealthResult[H]]
  ): HealthResult[Report[G, H, ?]] = {
    HealthResult(Report.fromResults[G, H, Health](results.map(_.value)))
  }
}
