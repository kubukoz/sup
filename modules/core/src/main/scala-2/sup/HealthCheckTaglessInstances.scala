package sup

import cats.tagless.FunctorK
import cats.{~>, Functor}

trait HealthCheckTaglessInstances {

  implicit def functorK[F[_]: Functor]: FunctorK[HealthCheck[F, *[_]]] = new FunctorK[HealthCheck[F, *[_]]] {
    override def mapK[G[_], H[_]](fgh: HealthCheck[F, G])(gh: G ~> H): HealthCheck[F, H] = fgh.mapK(gh)
  }
}
