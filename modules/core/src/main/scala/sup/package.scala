import sup.data.Report

package object sup {
  type HealthReporter[F[_], G[_], H[_]] = HealthCheck[F, Report[G, H, *]]

  type HealthCheckMod[F[_], H[_], G[_], I[_]] = HealthCheck[F, H] => HealthCheck[G, I]

  //A HealthCheckMod that does't change any types.
  //missed opportunity for a pun by not calling the above a "mondo"
  type HealthCheckEndoMod[F[_], H[_]] = HealthCheckMod[F, H, F, H]
}
