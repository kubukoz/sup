package sup.modules

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.{path => akkaPath, _}
import akka.http.scaladsl.server.{RequestContext, Route}
import cats.effect.Effect
import cats.syntax.functor._
import cats.syntax.reducible._
import cats.{Functor, Reducible, Semigroup, ~>}
import sup.{Health, HealthCheck, HealthResult}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object akkaHttp {

  /**
   * Builds a Route value that'll check the result of the healthcheck, and,
   * if it's sick, return ServiceUnavailable (Ok otherwise). See [[healthCheckResponse]]
   * for an alternative that doesn't provide a route matcher.
   *
   * The semigroup is used only to determine whether the overall health status is healthy.
   **/
  def healthCheckRoutes[F[_] : Effect, H[_] : Reducible](
    healthCheck: HealthCheck[F, H],
    path: String = "health-check")
  (implicit marshaller: ToEntityMarshaller[HealthResult[H]], combineHealthChecks: Semigroup[Health]): Route = {
    akkaPath(path) {
      get {
        onComplete(Effect[F].toIO(healthCheckResponse(healthCheck)).unsafeToFuture()) {
          case Success(response) => complete(response)
          case Failure(e) => failWith(e)
        }
      }
    }
  }

  def healthCheckResponse[F[_] : Functor, H[_] : Reducible](healthCheck: HealthCheck[F, H])(
    implicit combineHealthChecks: Semigroup[Health]): F[(StatusCode, HealthResult[H])] = {
    healthCheck.check.map { check =>
      if (check.value.reduce.isHealthy) StatusCodes.OK -> check
      else StatusCodes.ServiceUnavailable -> check
    }
  }

  def healthCheckRoutesWithContext[F[_]: Functor, H[_] : Reducible, R](
    healthCheck: HealthCheck[F, H],
    path: String = "health-check",
    f: RequestContext => F ~> Future)
  (implicit marshaller: ToEntityMarshaller[HealthResult[H]], combineHealthChecks: Semigroup[Health]): Route = {
    akkaPath(path) { requestCtx =>
      get {
        onComplete(f(requestCtx)(healthCheckResponse(healthCheck))) {
          case Success(response) => complete(response)
          case Failure(e) => failWith(e)
        }
      }(requestCtx)
    }
  }
}