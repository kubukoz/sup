import cats.data.{NonEmptyList, OneAnd}
import sup.data.Tagged

package object sup {
  type ∘[F[_], G[_]] = { type λ[A] = F[G[A]] }

  //for 2.11 compatibility, this type alias must be next to the companion's alias
  type HealthReporter[F[_], G[_]] = HealthCheck[F, OneAnd[G, ?]]
  val HealthReporter: data.HealthReporter.type = data.HealthReporter

  type TaggedNel[Tag, A] = NonEmptyList[Tagged[Tag, A]]

  type HealthCheckMod[F[_], H[_], G[_], I[_]] = HealthCheck[F, H] => HealthCheck[G, I]

  //A HealthCheckMod that does't change any types.
  //missed opportunity for a pun by not calling the above a "mondo"
  type HealthCheckEndoMod[F[_], H[_]] = HealthCheckMod[F, H, F, H]
}
