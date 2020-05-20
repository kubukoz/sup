package sup.data

import cats.implicits._
import cats.Eq
import cats.Eval
import cats.Id
import cats.Reducible
import cats.Show
import cats.~>
import sup.Health
import cats.Foldable
import sup.data.TaggedT.Incorrect
import sup.data.TaggedT.Correct
import sup.HealthCheck
import cats.effect.IO
import sup.HealthResult

sealed trait TaggedT[+L, +R] extends Product with Serializable {

  def fold[A](incorrect: L => A, correct: => A): A = this match {
    case Incorrect(error, _) => incorrect(error)
    case Correct(_)          => correct
  }

  def toEither: Either[L, Unit] = fold(Left(_), Right(()))
}

object TaggedT {
  final case class Incorrect[L, R](error: L, value: R) extends TaggedT[L, R]
  final case class Correct[R](value: R) extends TaggedT[Nothing, R]

  def incorrect[L](error: L): TaggedT[L, Health] = Incorrect(error, Health.Sick)
  val correct: TaggedT[Nothing, Health] = Correct(Health.Healthy)

  implicit def catsReducibleForTaggedT[L]: Reducible[TaggedT[L, *]] =
    new Reducible[TaggedT[L, *]] {

      def foldLeft[A, B](fa: TaggedT[L, A], b: B)(f: (B, A) => B): B =
        fa match {
          case Incorrect(_, a) => f(b, a)
          case Correct(a)      => f(b, a)
        }

      def foldRight[A, B](fa: TaggedT[L, A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = fa match {
        case Incorrect(_, a) => f(a, lb)
        case Correct(a)      => f(a, lb)
      }

      def reduceLeftTo[A, B](fa: TaggedT[L, A])(f: A => B)(g: (B, A) => B): B =
        fa match {
          case Incorrect(_, a) => f(a)
          case Correct(a)      => f(a)
        }

      def reduceRightTo[A, B](fa: TaggedT[L, A])(f: A => B)(g: (A, Eval[B]) => Eval[B]): Eval[B] = fa match {
        case Incorrect(_, a) => Eval.later(f(a))
        case Correct(a)      => Eval.later(f(a))
      }

    }

}

object Demo extends App {
  final case class MyErr(msg: String)
  implicit val showMyErr: Show[MyErr] = e => show"MyErr(${e.msg})"

  def demoL: TaggedT[MyErr, Health] = TaggedT.incorrect(MyErr("woops"))
  def demoR: TaggedT[MyErr, Health] = TaggedT.correct

  implicit def show[A: Show, B]: Show[TaggedT[A, B]] = t => t.fold("Sick: " + _.show, "Healthy")
  println(demoL.reduce) //Sick
  println(demoR.reduce) //Healthy
  println(demoL.show) //Sick: MyErr
  println(demoR.show) //Healthy

  val hc1 = HealthCheck.liftF(IO(HealthResult(demoL)))
  val hc2 = HealthCheck.liftF(IO(HealthResult(demoR)))

  val reporter = HealthReporter.fromChecks(hc1, hc2)
  println(reporter.check.unsafeRunSync().value.health)
  println(reporter.check.unsafeRunSync().value.checks.map(_.toEither))
}

final case class Tagged[Tag, H](tag: Tag, health: H)

object Tagged {

  /**
    * The Tagged instance of Foldable.
    * The only place where it should be passed to is [[sup.HealthReporter.fromChecks]],
    * for determining the status of the wrapping check. In other cases, it's probably useless, as it discards the tag completely.
    * */
  implicit def catsReducibleForTagged[Tag]: Reducible[Tagged[Tag, ?]] = Reducibles.by(λ[Tagged[Tag, ?] ~> Id](_.health))

  implicit def catsEqForTagged[Tag: Eq, H: Eq]: Eq[Tagged[Tag, H]] = Eq.by(tagged => (tagged.tag, tagged.health))

  implicit def catsShowForTagged[Tag: Show, H: Show]: Show[Tagged[Tag, H]] =
    Show.show(t => show"Tagged(tag = ${t.tag}, health = ${t.health}")

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
