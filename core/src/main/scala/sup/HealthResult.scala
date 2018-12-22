package sup

import cats.implicits._
import cats.kernel.Monoid
import cats.{~>, Applicative, Id}
import sup.algebra.FunctorK
import sup.data.Tagged

final case class HealthResult[H[_]](value: H[Health]) extends AnyVal

object HealthResult {
  def const[H[_]: Applicative](health: Health): HealthResult[H] = HealthResult(health.pure[H])

  def one(head: Health): HealthResult[Id]                               = HealthResult[Id](head)
  def tagged[Tag](tag: Tag, head: Health): HealthResult[Tagged[Tag, ?]] = HealthResult(Tagged(tag, head))

  implicit val functorK: FunctorK[HealthResult] = new FunctorK[HealthResult] {
    def mapK[F[_], G[_]](hf: HealthResult[F])(fg: F ~> G): HealthResult[G] = new HealthResult[G](fg(hf.value))
  }

  implicit def monoid[H[_]: Applicative](implicit M: Monoid[Health]): Monoid[HealthResult[H]] =
    new Monoid[HealthResult[H]] {
      override val empty: HealthResult[H] = HealthResult.const[H](M.empty)
      override def combine(x: HealthResult[H], y: HealthResult[H]): HealthResult[H] =
        HealthResult(Applicative.monoid[H, Health].combine(x.value, y.value))
    }
}
