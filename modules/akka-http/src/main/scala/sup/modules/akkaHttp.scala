package sup.modules

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{path => akkaPath, _}
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.Route
import cats.effect.Effect
import cats.syntax.functor._
import cats.syntax.reducible._
import cats.~>
import cats.Functor
import cats.Reducible
import sup.HealthCheck
import sup.HealthResult

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import akka.http.scaladsl.model.HttpRequest

object akkahttp {

  /**
    * Builds a Route value that'll check the result of the healthcheck, and,
    * if it's sick, return ServiceUnavailable (Ok otherwise). See [[healthCheckResponse]]
    * for an alternative that doesn't provide a route matcher.
   **/
  def healthCheckRoutes[F[_]: Effect, H[_]: Reducible](
    healthCheck: HealthCheck[F, H],
    path: String = "health-check"
  )(
    implicit marshaller: ToEntityMarshaller[HealthResult[H]]
  ): Route =
    akkaPath(path) {
      get {
        onComplete(Effect[F].toIO(healthCheckResponse(healthCheck)).unsafeToFuture()) {
          case Success(response) => complete(response)
          case Failure(error)    => failWith(error)
        }
      }
    }

  def healthCheckResponse[F[_]: Functor, H[_]: Reducible](
    healthCheck: HealthCheck[F, H]
  ): F[(StatusCode, HealthResult[H])] =
    healthCheck.check.map { check =>
      if (check.value.reduce.isHealthy) StatusCodes.OK -> check
      else StatusCodes.ServiceUnavailable -> check
    }

  def healthCheckRoutesWithContext[F[_]: Functor, H[_]: Reducible, R](
    healthCheck: HealthCheck[F, H],
    path: String = "health-check"
  )(
    run: HttpRequest => F ~> Future
  )(
    implicit marshaller: ToEntityMarshaller[HealthResult[H]]
  ): Route =
    akkaPath(path) {
      get {
        extractRequest { request =>
          onComplete(run(request)(healthCheckResponse(healthCheck))) {
            case Success(response) => complete(response)
            case Failure(error)    => failWith(error)
          }
        }
      }
    }
}
