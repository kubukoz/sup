package sup.modules

import cats.{Id, Monad}
import eu.timepit.refined.types.numeric.PosInt
import sup.HealthCheck
import cats.effect.Bracket

object doobie {
  import _root_.doobie._
  import _root_.doobie.implicits._

  /**
    * A healthcheck that checks whether a connection produced by the transactor is valid.
    * If `timeoutSeconds` is empty, there's no timeout. You should probably have a timeout, though.
    *
    * Note: Errors aren't recovered in this healthcheck. If you want error handling,
    * consider using [[HealthCheck.through]] with [[sup.mods.recoverToSick]].
    * */
  def connectionCheck[F[_]: Bracket[?[_], Throwable]](xa: Transactor[F])(timeoutSeconds: Option[PosInt]): HealthCheck[F, Id] = {
    //zero means infinite in JDBC
    val actualTimeoutSeconds = timeoutSeconds.fold(0)(_.value)

    HealthCheck.liftFBoolean {
      FC.isValid(actualTimeoutSeconds).transact(xa)
    }
  }
}
