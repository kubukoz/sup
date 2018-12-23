package sup
import _root_.scalacache._
import cats.Endo

import scala.concurrent.duration.Duration

object scalacache {

  /**
    * Use with [[HealthCheck.transform]].
    * */
  def cached[F[_], H[_]](key: String, ttl: Option[Duration])(implicit cache: Cache[HealthResult[H]],
                                                             mode: Mode[F],
                                                             flags: Flags): HealthCheckMod[F, H, F, H] = _.transform {
    action =>
      cache.cachingForMemoizeF(key)(ttl)(action)
  }
}
