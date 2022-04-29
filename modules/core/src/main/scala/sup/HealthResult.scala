package sup

import cats.syntax.all._
import cats.kernel.Monoid
import cats.~>
import cats.Applicative
import cats.Eq
import cats.Id
import sup.data.Tagged

final case class HealthResult[H[_]](value: H[Health]) extends AnyVal {
  def transform[I[_]](f: H[Health] => I[Health]): HealthResult[I] = HealthResult(f(value))

  def mapK[G[_]](fg: H ~> G): HealthResult[G] = new HealthResult[G](fg(value))
}

object HealthResult extends HealthResultTaglessInstances {
  def const[H[_]: Applicative](health: Health): HealthResult[H] = HealthResult(health.pure[H])

  val one: Health => HealthResult[Id] = HealthResult[Id]
  def tagged[Tag](tag: Tag, head: Health): HealthResult[Tagged[Tag, *]] = HealthResult(Tagged(tag, head))

  implicit def monoid[H[_]: Applicative](implicit M: Monoid[Health]): Monoid[HealthResult[H]] =
    Monoid.instance(
      HealthResult.const[H](M.empty),
      (x, y) => HealthResult(Applicative.monoid[H, Health].combine(x.value, y.value))
    )

  implicit def eq[H[_]](implicit H: Eq[H[Health]]): Eq[HealthResult[H]] = Eq.by(_.value)
}
