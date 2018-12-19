package sup

import cats.data.NonEmptyList
import cats.Applicative
import cats.implicits._

trait HealthReporter[F[_]] {
  def report: F[HealthReport]
}

object HealthReporter {

  def fromChecks[F[_]: Applicative](first: HealthCheck[F], rest: HealthCheck[F]*): HealthReporter[F] =
    new HealthReporter[F] {
      override val report: F[HealthReport] = {
        NonEmptyList(first, rest.toList).traverse(_.check).map {
          case results if results.forall(_.isGood) => HealthReport.Good(results)
          case results                             => HealthReport.Bad(results)
        }
      }
    }
}

sealed trait HealthReport extends Product with Serializable {
  def checks: NonEmptyList[Health]
}

object HealthReport {
  case class Good(checks: NonEmptyList[Health]) extends HealthReport
  case class Bad(checks: NonEmptyList[Health]) extends HealthReport
}
