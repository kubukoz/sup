import cats.data.{NonEmptyList, OneAnd}
import sup.data.Tagged

package object sup {
  type ∘[F[_], G[_]] = { type λ[A] = F[G[A]] }

  type HealthReporter[F[_], G[_]] = HealthCheck[F, OneAnd[G, ?]]

  type TaggedNel[Tag, A] = NonEmptyList[Tagged[Tag, A]]
}
