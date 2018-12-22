package sup.alg

import cats.~>
import simulacrum._

@typeclass
trait FunctorK[F[_[_]]] {
  def mapK[G[_], H[_]](fgh: F[G])(gh: G ~> H): F[H]
}
