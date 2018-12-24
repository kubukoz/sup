package sup.http4s

import cats.effect.Sync
import cats.{Monad, Reducible}
import sup.{Health, HealthCheck, HealthResult}
import org.http4s.{EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import cats.kernel.Semigroup

trait HealthCheckRoutes[F[_]] {
  def routes: HttpRoutes[F]
}

object HealthCheckRoutes {

  def instance[F[_]: Sync, H[_]: Reducible](
    healthCheck: HealthCheck[F, H],
    path: String = "health-check"
  )(implicit encoder: EntityEncoder[F, HealthResult[H]], combineHealthChecks: Semigroup[Health]): HealthCheckRoutes[F] =
    new HealthCheckRoutes[F] with Http4sDsl[F] {

      override val routes: HttpRoutes[F] = HttpRoutes.of[F] {
        case GET -> Root / `path` =>
          healthCheck.check.flatMap { check =>
            if (check.value.fold.isHealthy) Ok(check)
            else ServiceUnavailable(check)
          }
      }
    }
}
