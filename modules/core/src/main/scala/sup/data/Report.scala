package sup.data

import cats.data.Nested
import cats.{Foldable, NonEmptyReducible, Reducible}

/**
  * A more specific version of [[cats.data.OneAnd]] combined with [[cats.data.Nested]].
  * */
case class Report[G[_], H[_], A](health: A, checks: G[H[A]])

object Report extends ReportInstances

trait ReportInstances {
  implicit def catsReducibleForReport[G[_], H[_]](implicit F: Foldable[Nested[G, H, ?]]): Reducible[Report[G, H, ?]] =
    new ReportReducible[G, H]
}

private[data] class ReportReducible[G[_], H[_]](implicit F: Foldable[Nested[G, H, ?]])
    extends NonEmptyReducible[Report[G, H, ?], Nested[G, H, ?]] {
  override def split[A](fa: Report[G, H, A]): (A, Nested[G, H, A]) = (fa.health, Nested(fa.checks))
}
