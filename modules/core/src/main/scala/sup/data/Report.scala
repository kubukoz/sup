package sup.data

import cats.data.Nested
import cats.Foldable
import cats.NonEmptyReducible
import cats.Reducible
import cats.Show
import cats.implicits._
import cats.kernel.Monoid

/**
  * A more specific version of [[cats.data.OneAnd]] combined with [[cats.data.Nested]].
  * */
case class Report[G[_], H[_], A](health: A, checks: G[H[A]])

object Report extends ReportInstances {

  def fromResults[G[_]: Reducible, H[_]: Reducible, A: Monoid](results: G[H[A]]): Report[G, H, A] =
    Report(results.reduceMap(_.reduce), results)
}

trait ReportInstances {
  implicit def catsReducibleForReport[G[_], H[_]](implicit F: Foldable[Nested[G, H, ?]]): Reducible[Report[G, H, ?]] =
    new ReportReducible[G, H]

  implicit def catsShowForReport[G[_], H[_], A: Show](implicit showGha: Show[G[H[A]]]): Show[Report[G, H, A]] =
    Show.show { report =>
      show"Report(health = ${report.health}, checks = ${report.checks})"
    }
}

private[data] class ReportReducible[G[_], H[_]](implicit F: Foldable[Nested[G, H, ?]])
    extends NonEmptyReducible[Report[G, H, ?], Nested[G, H, ?]] {
  override def split[A](fa: Report[G, H, A]): (A, Nested[G, H, A]) = (fa.health, Nested(fa.checks))
}
