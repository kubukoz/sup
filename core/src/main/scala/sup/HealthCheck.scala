package sup

trait HealthCheck[F[_]] {
  def check: F[Health]
}
