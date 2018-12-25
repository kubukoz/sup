package sup.data

import cats.{Foldable, NonEmptyReducible, Reducible}

/**
  * A more specific version of [[cats.data.OneAnd]].
  * */
case class Report[H[_], A](health: A, checks: H[A])

object Report extends ReportInstances

trait ReportInstances {
  implicit def catsReducibleForReport[H[_]: Foldable]: Reducible[Report[H, ?]] =
    new ReportReducible[H]
}

private[data] class ReportReducible[H[_]: Foldable] extends NonEmptyReducible[Report[H, ?], H] {
  override def split[A](fa: Report[H, A]): (A, H[A]) = (fa.health, fa.checks)
}
