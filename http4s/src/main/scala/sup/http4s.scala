package sup

import cats.effect.Sync
import cats.implicits._
import cats.kernel.Semigroup
import cats.{Monad, Reducible}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes, Response}

object http4s {

  /**
    * Builds a HttpRoutes value that'll check the result of the healthcheck, and,
    * if it's sick, return ServiceUnavailable (Ok otherwise). See [[healthCheckResponse]]
    * for an alternative that doesn't provide a route matcher.
    *
    * The semigroup is used only to determine whether the overall health status is healthy.
    * */
  def healthCheckRoutes[F[_]: Sync, H[_]: Reducible](
    healthCheck: HealthCheck[F, H],
    path: String = "health-check"
  )(implicit encoder: EntityEncoder[F, HealthResult[H]], combineHealthChecks: Semigroup[Health]): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / `path` =>
        healthCheckResponse(healthCheck)
    }
  }

  def healthCheckResponse[F[_]: Monad, H[_]: Reducible](healthCheck: HealthCheck[F, H])(
    implicit encoder: EntityEncoder[F, HealthResult[H]],
    combineHealthChecks: Semigroup[Health]): F[Response[F]] = {

    val dsl = new Http4sDsl[F] {}
    import dsl._

    healthCheck.check.flatMap { check =>
      if (check.value.reduce.isHealthy) Ok(check)
      else ServiceUnavailable(check)
    }
  }
}
