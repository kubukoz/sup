package sup

import cats.Id
import cats.effect.Sync
import eu.timepit.refined.types.numeric.PosInt

object doobie {
  import _root_.doobie.implicits._
  import _root_.doobie._

  /**
    * A healthcheck that checks whether a connection produced by the transactor is valid.
    * If `timeoutSeconds` is empty, there's no timeout.
    * */
  def connectionCheck[F[_]: Sync](xa: Transactor[F])(timeoutSeconds: Option[PosInt]): HealthCheck[F, Id] = {
    //zero means infinite in JDBC
    val actualTimeoutSeconds = timeoutSeconds.fold(0)(_.value)

    HealthCheck.liftF {
      FC.isValid(actualTimeoutSeconds).map(HealthResult.one compose Health.fromBoolean).transact(xa)
    }
  }
}
