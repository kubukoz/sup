package sup.modules

import _root_.sttp.client3.Request
import _root_.sttp.client3.SttpBackend
import cats.Functor
import cats.Id
import cats.implicits._
import sup.HealthCheck
import sup.HealthResult

object sttp {

  /** Checks if a request made with a client has a successful status (1xx-3xx).
    */
  def statusCodeHealthCheck[F[_]: Functor, T](
    call: Request[T, Nothing]
  )(
    implicit backend: SttpBackend[F, Nothing]
  ): HealthCheck[F, Id] =
    HealthCheck.liftFBoolean {
      backend.send(call).map(response => !response.isClientError && !response.isServerError)
    }

  /** Fetches the result of a remote health check.
    */
  def remoteHealthCheck[F[_], H[_]](
    call: Request[HealthResult[H], Nothing]
  )(
    implicit backend: SttpBackend[F, Nothing]
  ): HealthCheck[F, H] =
    HealthCheck.liftF {
      backend.responseMonad.map(backend.send(call))(_.body)
    }
}
