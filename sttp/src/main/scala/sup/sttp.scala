package sup

import cats.Functor
import cats.effect.Sync
import cats.implicits._
import com.softwaremill.sttp._

object sttp {

  /**
    * Checks if a request made with a client has a successful status (1xx-3xx).
    * */
  def statusCodeHealthCheck[F[_]: Functor, T](call: Request[T, Nothing])(
    implicit backend: SttpBackend[F, Nothing]): HealthCheck[F, Id] = {
    HealthCheck.liftFBoolean {
      backend.send(call).map { response =>
        !response.isClientError && !response.isServerError
      }
    }
  }

  /**
    * Fetches the result of a remote health check.
    * */
  def remoteHealthCheck[F[_]: Sync, H[_]](call: Request[HealthResult[H], Nothing])(
    implicit backend: SttpBackend[F, Nothing]): HealthCheck[F, H] = {
    HealthCheck.liftF {
      backend.send(call).flatMap(response => Sync[F].delay(response.unsafeBody))
    }
  }
}
