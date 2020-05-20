package sup

import cats.implicits._
import cats.kernel.Monoid
import cats.tagless.FunctorK
import cats.~>
import cats.Applicative
import cats.Eq
import cats.Id
import sup.data.Tagged

final case class HealthResult[H[_]](value: H[Health]) extends AnyVal {
  def transform[I[_]](f: H[Health] => I[Health]): HealthResult[I] = HealthResult(f(value))
}

object HealthResult {
  def const[H[_]: Applicative](health: Health): HealthResult[H] = HealthResult(health.pure[H])

  val one: Health => HealthResult[Id] = HealthResult[Id]
  def tagged[Tag](tag: Tag, head: Health): HealthResult[Tagged[Tag, ?]] = HealthResult(Tagged(tag, head))

  implicit val functorK: FunctorK[HealthResult] = new FunctorK[HealthResult] {
    def mapK[F[_], G[_]](hf: HealthResult[F])(fg: F ~> G): HealthResult[G] = new HealthResult[G](fg(hf.value))
  }

  implicit def monoid[H[_]: Applicative]: Monoid[HealthResult[H]] =
    new Monoid[HealthResult[H]] {
      override val empty: HealthResult[H] = HealthResult.const[H](Monoid.empty[Health])

      override def combine(x: HealthResult[H], y: HealthResult[H]): HealthResult[H] =
        HealthResult(Applicative.monoid[H, Health].combine(x.value, y.value))
    }

  implicit def eq[H[_]](implicit H: Eq[H[Health]]): Eq[HealthResult[H]] = Eq.by(_.value)
}
