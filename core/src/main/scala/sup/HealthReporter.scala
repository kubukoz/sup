package sup

import cats.data.NonEmptyList
import cats.implicits._
import cats.{Apply, Foldable, NonEmptyTraverse}
import cats.data.OneAnd

object HealthReporter {

  type HealthReporter[F[_], G[_]] = HealthCheck[F, OneAnd[G, ?]]

  def fromChecks[F[_]: Apply, H[_]: Foldable](first: HealthCheck[F, H],
                                              rest: HealthCheck[F, H]*): HealthReporter[F, (NonEmptyList ∘ H)#λ] =
    wrapChecks(NonEmptyList(first, rest.toList))

  /**
    * Constructs a healthcheck from a non-empty structure of healthchecks.
    * If any of the healthchecks fails, the status of the whole check is failed.
    *
    * Note: Checks inside `H[_]` (which will usually be Id or a tagged alternative to Id)
    * are combined using the all-good monoid - they'll be concatenated, and any failure also fails the check.
    */
  def wrapChecks[F[_]: Apply, G[_]: NonEmptyTraverse, H[_]: Foldable](
    checks: G[HealthCheck[F, H]]): HealthReporter[F, (G ∘ H)#λ] = {
    type GH[A] = G[H[A]]

    new HealthReporter[F, GH] {
      override val check: F[HealthResult[OneAnd[GH, ?]]] = {
        checks.nonEmptyTraverse(_.check).map { results =>
          val status = if (results.forall(HealthResult.combineAllGood[H](_).isGood)) Health.Good else Health.Bad

          HealthResult[OneAnd[GH, ?]](OneAnd[GH, Health](status, results.map(_.value)))
        }
      }
    }
  }
}
