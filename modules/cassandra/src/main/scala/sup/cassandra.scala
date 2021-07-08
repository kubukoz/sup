package sup

import cats.Id
import cats.effect.{Blocker, ContextShift, Sync}
import com.datastax.oss.driver.api.core.CqlSession

object cassandra {

  def connectionCheck[F[_] : Sync : ContextShift](session: CqlSession)(implicit blocker: Blocker): HealthCheck[F, Id] =
    HealthCheck.liftFBoolean {
      blocker.delay(session
        .execute("SELECT now() FROM system.local").isFullyFetched
      )
    }
}
