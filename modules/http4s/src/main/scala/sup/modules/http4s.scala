package sup.modules

import cats.effect.Sync
import cats.Monad
import cats.Reducible
import org.http4s.dsl.Http4sDsl
import org.http4s.EntityEncoder
import org.http4s.HttpRoutes
import org.http4s.Response
import sup.HealthCheck
import sup.HealthResult
import cats.implicits._

object http4s {

  /**
    * Builds a HttpRoutes value that'll check the result of the healthcheck, and,
    * if it's sick, return ServiceUnavailable (Ok otherwise). See [[healthCheckResponse]]
    * for an alternative that doesn't provide a route matcher.
    * */
  def healthCheckRoutes[F[_]: Sync, H[_]: Reducible](
    healthCheck: HealthCheck[F, H],
    path: String = "health-check"
  )(
    implicit encoder: EntityEncoder[F, HealthResult[H]]
  ): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / `path` =>
        healthCheckResponse(healthCheck)
    }
  }

  def healthCheckResponse[F[_]: Monad, H[_]: Reducible](
    healthCheck: HealthCheck[F, H]
  )(
    implicit encoder: EntityEncoder[F, HealthResult[H]]
  ): F[Response[F]] = {

    val dsl = new Http4sDsl[F] {}
    import dsl._

    healthCheck.check.flatMap { check =>
      if (check.value.reduce.isHealthy) Ok(check)
      else ServiceUnavailable(check)
    }
  }
}
