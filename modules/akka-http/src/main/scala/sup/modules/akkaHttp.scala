package sup.modules.akkahttp

import scala.annotation.implicitNotFound
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.{path => akkaPath}
import akka.http.scaladsl.server.Route
import cats.Functor
import cats.Reducible
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.functor._
import cats.syntax.reducible._
import cats.~>
import sup.HealthCheck
import sup.HealthResult
import akka.http.scaladsl.server.PathMatcher

@implicitNotFound("Couldn't find an AkkaSup[$F]. You can create one in a Resource using AkkaSup.instance[$F].")
trait AkkaSup[F[_]] {

  def healthCheckRoutes[H[_]: Reducible](
    healthCheck: HealthCheck[F, H],
    path: PathMatcher[Unit] = "health-check"
  )(
    implicit marshaller: ToEntityMarshaller[HealthResult[H]]
  ): Route
}

object AkkaSup {
  def apply[F[_]](implicit F: AkkaSup[F]): AkkaSup[F] = F

  def instance[F[_]: Async]: Resource[F, AkkaSup[F]] = Dispatcher[F].map { dispatcher =>
    new AkkaSup[F] {
      def healthCheckRoutes[H[_]: Reducible](
        healthCheck: HealthCheck[F, H],
        path: PathMatcher[Unit]
      )(
        implicit marshaller: ToEntityMarshaller[HealthResult[H]]
      ): Route = AkkaSup.healthCheckRoutes(dispatcher, healthCheck, path)
    }
  }

  /** Builds a Route value that'll check the result of the healthcheck, and,
    * if it's sick, return ServiceUnavailable (Ok otherwise). See [[healthCheckResponse]]
    * for an alternative that doesn't provide a route matcher.
    *
    * If you don't want to manage Dispatcher yourself, use `AkkaSup.instance[F].use { akkaSup => ... }`
    * and build the routes using `akkaSup.healthCheckRoutes` instead.
    */
  def healthCheckRoutes[F[_]: Functor, H[_]: Reducible](
    dispatcher: Dispatcher[F],
    healthCheck: HealthCheck[F, H],
    path: PathMatcher[Unit] = "health-check"
  )(
    implicit marshaller: ToEntityMarshaller[HealthResult[H]]
  ): Route =
    akkaPath(path) {
      get {
        onComplete(dispatcher.unsafeToFuture(healthCheckResponse(healthCheck))) {
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
