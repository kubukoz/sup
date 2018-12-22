package sup

import cats.{~>, Eval, Foldable, Functor, Id}
import cats.implicits._
import sup.alg.FunctorK

case class HealthResult[H[_]](value: H[Health]) extends AnyVal

object HealthResult {
  def one(head: Health): HealthResult[Id]                               = HealthResult[Id](head)
  def tagged[Tag](tag: Tag, head: Health): HealthResult[Tagged[Tag, ?]] = HealthResult(Tagged(tag, head))

  def combineAllGood[H[_]: Foldable](healthResult: HealthResult[H]): Health =
    healthResult.value.combineAll(Health.allGoodMonoid)
}

trait HealthCheck[F[_], H[_]] {
  def check: F[HealthResult[H]]
}

object HealthCheck {
  implicit def functorK[F[_]: Functor]: FunctorK[HealthCheck[F, ?[_]]] = new FunctorK[HealthCheck[F, ?[_]]] {
    override def mapK[G[_], H[_]](fgh: HealthCheck[F, G])(gh: G ~> H): HealthCheck[F, H] =
      new HealthCheck[F, H] {
        override val check: F[HealthResult[H]] = fgh.check.map(res => HealthResult(gh(res.value)))
      }
  }
}

//final class ModifiedHealthCheck[F[_]](underlying: HealthCheck[F], modifier: HealthCheckMod[F]) extends HealthCheck[F] {
//  override val check: F[Health] = modifier.modify(underlying.check)
//  override def modify(mod: HealthCheckMod[F]): HealthCheck[F] = new ModifiedHealthCheck[F](underlying, mod |+| modifier)
//}


case class Tagged[Tag, H](tag: Tag, health: H)

object Tagged {
  def tagK[Tag](tag: Tag): Id ~> Tagged[Tag, ?] = λ[Id ~> Tagged[Tag, ?]](Tagged(tag, _))

  implicit def taggedFoldable[Tag]: Foldable[Tagged[Tag, ?]] = new Foldable[Tagged[Tag, ?]] {
    def foldLeft[A, B](fa: Tagged[Tag, A], b: B)(f: (B, A) => B): B = f(b, fa.health)

    def foldRight[A, B](fa: Tagged[Tag, A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
      f(fa.health, lb)
  }
}
