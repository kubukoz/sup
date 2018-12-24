package sup

import cats.{ApplicativeError, Id}
import com.github.gvolpe.fs2redis.algebra.Ping
import cats.implicits._

object redis {

  /**
    * Creates a healthcheck for an fs2-redis connection. If the check fails, the result is [[Health.Sick]].
    * */
  def pingCheck[F[_], E](implicit cmd: Ping[F], F: ApplicativeError[F, E]): HealthCheck[F, Id] = HealthCheck.liftF {
    cmd.ping.as(Health.healthy).orElse(Health.sick.pure[F]).map(HealthResult.one)
  }
}
