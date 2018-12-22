package sup.data

import cats.{Eval, Foldable, Id, ~>}

final case class Tagged[Tag, H](tag: Tag, health: H)

object Tagged {
  def tagK[Tag](tag: Tag): Id ~> Tagged[Tag, ?] = λ[Id ~> Tagged[Tag, ?]](Tagged(tag, _))

  implicit def taggedFoldable[Tag]: Foldable[Tagged[Tag, ?]] = new Foldable[Tagged[Tag, ?]] {
    def foldLeft[A, B](fa: Tagged[Tag, A], b: B)(f: (B, A) => B): B = f(b, fa.health)

    def foldRight[A, B](fa: Tagged[Tag, A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
      f(fa.health, lb)
  }
}
