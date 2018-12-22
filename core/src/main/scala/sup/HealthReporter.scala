package sup

import cats.data.{NonEmptyList, OneAnd}
import cats.implicits._
import cats.{Apply, Foldable, Monoid, NonEmptyTraverse}

object HealthReporter {

  /**
    * Equivalent to [[wrapChecks]] for G = NonEmptyList, with more pleasant syntax.
    * */
  def fromChecks[F[_]: Apply, H[_]: Foldable](first: HealthCheck[F, H], rest: HealthCheck[F, H]*)(
    implicit M: Monoid[Health]): HealthReporter[F, (NonEmptyList ∘ H)#λ] =
    wrapChecks(NonEmptyList(first, rest.toList))

  /**
    * Constructs a healthcheck from a non-empty structure G of healthchecks.
    * The status of the whole check is determined using the results of given checks and the monoid of Health.
    *
    * e.g. if all checks need to return Healthy for the whole thing to be healthy, use [[Health.allHealthyMonoid]].
    */
  def wrapChecks[F[_]: Apply, G[_]: NonEmptyTraverse, H[_]: Foldable](checks: G[HealthCheck[F, H]])(
    implicit M: Monoid[Health]): HealthReporter[F, (G ∘ H)#λ] = {
    type GH[A] = G[H[A]]

    new HealthReporter[F, GH] {
      override val check: F[HealthResult[OneAnd[GH, ?]]] = {
        checks.nonEmptyTraverse(_.check).map { results =>
          val status =
            if (results.reduceMap(_.value.combineAll).isHealthy) Health.Healthy else Health.Sick

          HealthResult[OneAnd[GH, ?]](OneAnd[GH, Health](status, results.map(_.value)))
        }
      }
    }
  }
}
