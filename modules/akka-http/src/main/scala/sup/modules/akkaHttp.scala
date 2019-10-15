package sup.modules

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{path => akkaPath, _}
import akka.http.scaladsl.server.Route
import cats.Reducible
import cats.effect.Effect
import cats.Semigroup
import cats.syntax.reducible._
import sup.{Health, HealthCheck, HealthResult}

import scala.util.{Failure, Success}

object akkaHttp {

  /**
   * Builds a Route value that'll check the result of the healthcheck, and,
   * if it's sick, return ServiceUnavailable (Ok otherwise).
   *
   * The semigroup is used only to determine whether the overall health status is healthy.
   **/
  def healthCheckRoutes[F[_] : Effect, H[_] : Reducible](
    healthCheck: HealthCheck[F, H],
    path: String = "health-check")
  (implicit marshaller: ToEntityMarshaller[HealthResult[H]], combineHealthChecks: Semigroup[Health]): Route = {
    akkaPath(path) {
      get {
        onComplete(Effect[F].toIO(healthCheck.check).unsafeToFuture()) {
          case Success(check) =>
            if (check.value.reduce.isHealthy) complete(StatusCodes.OK -> check)
            else complete(StatusCodes.ServiceUnavailable -> check)

          case Failure(e) =>
            failWith(e)
        }
      }
    }
  }
}