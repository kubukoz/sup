package sup.modules

import sup.{HealthCheckEndoMod, HealthResult}
import _root_.scalacache.{Cache, Flags, Mode}

import scala.concurrent.duration.Duration

object scalacache {

  /**
    * Caches a healthcheck for the given amount of time (or forever, if `ttl` is empty).
    * */
  def cached[F[_], H[_]](key: String, ttl: Option[Duration])(implicit cache: Cache[HealthResult[H]],
                                                             mode: Mode[F],
                                                             flags: Flags): HealthCheckEndoMod[F, H] = _.transform {
    action =>
      cache.cachingForMemoizeF(key)(ttl)(action)
  }
}
