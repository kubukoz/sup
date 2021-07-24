package sup

import cats.tagless.FunctorK
import cats.~>

trait HealthResultTaglessInstances {

  implicit val functorK: FunctorK[HealthResult] = new FunctorK[HealthResult] {
    def mapK[F[_], G[_]](hf: HealthResult[F])(fg: F ~> G): HealthResult[G] = hf.mapK(fg)
  }
}
