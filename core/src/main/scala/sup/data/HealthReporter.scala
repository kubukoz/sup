package sup.data

import cats.data.{NonEmptyList, OneAnd}
import cats.implicits._
import cats.kernel.Semigroup
import cats.{Apply, Foldable, NonEmptyTraverse}
import sup._

object HealthReporter {

  /**
    * Equivalent to [[wrapChecks]] for G = NonEmptyList, with more pleasant syntax.
    * */
  def fromChecks[F[_]: Apply, H[_]: Foldable](first: HealthCheck[F, H], rest: HealthCheck[F, H]*)(
    implicit M: Semigroup[Health]): HealthReporter[F, (NonEmptyList ∘ H)#λ] =
    wrapChecks(NonEmptyList(first, rest.toList))

  /**
    * Constructs a healthcheck from a non-empty structure G of healthchecks.
    * The status of the whole check is determined using the results of given checks and the semigroup of Health.
    *
    * e.g. if all checks need to return Healthy for the whole thing to be healthy, use [[Health.allHealthyCommutativeMonoid]].
    */
  def wrapChecks[F[_]: Apply, G[_]: NonEmptyTraverse, H[_]: Foldable](checks: G[HealthCheck[F, H]])(
    implicit M: Semigroup[Health]): HealthReporter[F, (G ∘ H)#λ] = {
    type GH[A] = G[H[A]]

    new HealthReporter[F, GH] {
      override val check: F[HealthResult[OneAnd[GH, ?]]] = {
        checks.nonEmptyTraverse(_.check).map { results =>
          val status = results.reduceMap(_.value.combineAll)

          HealthResult(OneAnd[GH, Health](status, results.map(_.value)))
        }
      }
    }
  }
}
