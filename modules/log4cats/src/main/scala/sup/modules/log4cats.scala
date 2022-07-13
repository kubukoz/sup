package sup.modules

import cats.FlatMap
import cats.Show
import cats.syntax.all._
import org.typelevel.log4cats.MessageLogger
import sup.Health
import sup.HealthCheckEndoMod
import sup.mods

object log4cats {

  /** Wraps a healthcheck with a debug log message before and after making the call.
    * See [[mods.surround]] for a more customizable version.
    */
  def logged[F[_]: MessageLogger: FlatMap, H[_]](
    label: String
  )(
    implicit showH: Show[H[Health]]
  ): HealthCheckEndoMod[F, H] =
    mods.surround(MessageLogger[F].debug(show"Checking health for $label")) { result =>
      MessageLogger[F].debug(show"Health result for $label: ${result.value}")
    }
}
