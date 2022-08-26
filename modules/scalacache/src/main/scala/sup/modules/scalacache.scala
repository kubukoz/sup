package sup.modules

import scala.concurrent.duration.Duration

import _root_.scalacache.Cache
import _root_.scalacache.Flags
import sup.HealthCheckEndoMod
import sup.HealthResult

object scalacache {

  /** Caches a healthcheck for the given amount of time (or forever, if `ttl` is empty).
    */
  def cached[F[_], H[_]](
    key: String,
    ttl: Option[Duration]
  )(
    implicit cache: Cache[F, String, HealthResult[H]],
    flags: Flags
  ): HealthCheckEndoMod[F, H] = _.transform(cache.cachingF(key)(ttl))
}
