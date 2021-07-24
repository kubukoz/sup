package sup

import cats.Id
import cats.effect.kernel.Sync
import com.datastax.oss.driver.api.core.CqlSession

object cassandra {

  def connectionCheck[F[_]: Sync](session: CqlSession): HealthCheck[F, Id] =
    HealthCheck.liftFBoolean {
      Sync[F].delay(session.execute("SELECT now() FROM system.local").isFullyFetched)
    }
}
