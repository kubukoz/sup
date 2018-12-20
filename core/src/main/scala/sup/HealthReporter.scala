package sup

import cats.data.NonEmptyList
import cats.Applicative
import cats.implicits._

trait HealthReporter[F[_]] {
  def report: F[HealthReport]

  def mapChecks(hcc: HealthCheck[F] => HealthCheck[F]): HealthReporter[F]
}

object HealthReporter {

  def fromChecks[F[_]: Applicative](first: HealthCheck[F], rest: HealthCheck[F]*): HealthReporter[F] =
    new HealthReporter[F] {
      override val report: F[HealthReport] = {
        NonEmptyList(first, rest.toList).traverse(_.check).map { results =>
          val status = if (results.forall(_.isGood)) Health.Good else Health.Bad
          HealthReport(results, status)
        }
      }

      override def mapChecks(hcc: HealthCheck[F] => HealthCheck[F]): HealthReporter[F] =
        fromChecks(hcc(first), rest.map(hcc): _*)
    }
}
