package sup.data

import cats.{~>, Eq, Eval, Foldable}
import cats.instances.tuple._
import sup.mods

final case class Tagged[Tag, H](tag: Tag, health: H)

object Tagged {

  /**
    * The Tagged instance of Foldable.
    * The only place where it should be passed to is [[sup.HealthReporter.fromChecks]],
    * for determining the status of the wrapping check. In other cases, it's probably useless, as it discards the tag completely.
    * */
  implicit def taggedFoldable[Tag]: Foldable[Tagged[Tag, ?]] = Foldable2.by(mods.untag[Tag])

  implicit def eqTagged[Tag: Eq, H: Eq]: Eq[Tagged[Tag, H]] = Eq.by(tagged => (tagged.tag, tagged.health))

  //todo PR to cats
  object Foldable2 {

    def by[F[_], G[_]: Foldable](fg: F ~> G): Foldable[F] = new Foldable[F] {
      override def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B = Foldable[G].foldLeft(fg(fa), b)(f)
      override def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
        Foldable[G].foldRight(fg(fa), lb)(f)
    }
  }
}
