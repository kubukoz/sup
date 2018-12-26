package sup.modules

import cats.implicits._
import cats.{ApplicativeError, Id}
import com.github.gvolpe.fs2redis.algebra.Ping
import sup.{Health, HealthCheck, HealthResult}

object redis {

  /**
    * Creates a healthcheck for an fs2-redis connection. If the check fails, the result is [[Health.Sick]].
    * */
  def pingCheck[F[_], E](implicit cmd: Ping[F], F: ApplicativeError[F, E]): HealthCheck[F, Id] =
    HealthCheck.liftF {
      cmd.ping.as(HealthResult.one(Health.healthy))
    }.through(sup.mods.recoverToSick)
}
