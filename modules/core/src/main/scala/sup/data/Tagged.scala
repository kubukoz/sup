package sup.data

import cats.{~>, Eq, Eval, Foldable, Id, Reducible}
import cats.instances.tuple._
import sup.mods

final case class Tagged[Tag, H](tag: Tag, health: H)

object Tagged {

  /**
    * The Tagged instance of Foldable.
    * The only place where it should be passed to is [[sup.HealthReporter.fromChecks]],
    * for determining the status of the wrapping check. In other cases, it's probably useless, as it discards the tag completely.
    * */
  implicit def taggedFoldable[Tag]: Reducible[Tagged[Tag, ?]] = Reducibles.by(λ[Tagged[Tag, ?] ~> Id](_.health))

  implicit def eqTagged[Tag: Eq, H: Eq]: Eq[Tagged[Tag, H]] = Eq.by(tagged => (tagged.tag, tagged.health))

  object Reducibles {

    def by[F[_], G[_]: Reducible](fg: F ~> G): Reducible[F] = new Reducible[F] {
      override def reduceLeftTo[A, B](fa: F[A])(f: A => B)(g: (B, A) => B): B = Reducible[G].reduceLeftTo(fg(fa))(f)(g)
      override def reduceRightTo[A, B](fa: F[A])(f: A => B)(g: (A, Eval[B]) => Eval[B]): Eval[B] =
        Reducible[G].reduceRightTo(fg(fa))(f)(g)
      override def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B = Reducible[G].foldLeft(fg(fa), b)(f)
      override def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
        Reducible[G].foldRight(fg(fa), lb)(f)
    }
  }
}
