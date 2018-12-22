package object sup {
  type ∘[F[_], G[_]] = { type λ[A] = F[G[A]] }
}
