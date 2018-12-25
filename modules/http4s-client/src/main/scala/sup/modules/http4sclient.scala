package sup.modules

import cats.implicits._
import cats.{Functor, Id}
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Request}
import sup.{HealthCheck, HealthResult}

object http4sclient {

  /**
    * Checks if a request made with a client has a successful status (1xx-3xx).
    * */
  def statusCodeHealthCheck[F[_]: Functor](call: Request[F])(implicit client: Client[F]): HealthCheck[F, Id] =
    HealthCheck.liftFBoolean(client.status(call).map(_.isSuccess))

  /**
    * Fetches the result of a remote health check.
    * */
  def remoteHealthCheck[F[_], H[_]](call: Request[F])(implicit client: Client[F],
                                                      decoder: EntityDecoder[F, HealthResult[H]]): HealthCheck[F, H] =
    HealthCheck.liftF {
      client.expect[HealthResult[H]](call)
    }
}
