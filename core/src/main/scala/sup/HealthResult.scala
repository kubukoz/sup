package sup

import cats.implicits._
import cats.{Foldable, Id, ~>}
import sup.algebra.FunctorK
import sup.data.Tagged

final case class HealthResult[H[_]](value: H[Health]) extends AnyVal

object HealthResult {
  def one(head: Health): HealthResult[Id]                               = HealthResult[Id](head)
  def tagged[Tag](tag: Tag, head: Health): HealthResult[Tagged[Tag, ?]] = HealthResult(Tagged(tag, head))

  def combineAllGood[H[_]: Foldable](healthResult: HealthResult[H]): Health =
    healthResult.value.combineAll(Health.allHealthyMonoid)

  implicit val functorK: FunctorK[HealthResult] = new FunctorK[HealthResult] {
    def mapK[F[_], G[_]](hf: HealthResult[F])(fg: F ~> G): HealthResult[G] = new HealthResult[G](fg(hf.value))
  }
}
