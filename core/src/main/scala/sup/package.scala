import cats.data.OneAnd

package object sup {
  type ∘[F[_], G[_]] = { type λ[A] = F[G[A]] }

  type HealthReporter[F[_], G[_]] = HealthCheck[F, OneAnd[G, ?]]
}
