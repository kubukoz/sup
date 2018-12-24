package sup

import cats.{FlatMap, Show}
import io.chrisdavenport.log4cats.MessageLogger
import cats.implicits._

object log4cats {

  /**
    * Wraps a healthcheck with a debug log message before and after making the call.
    * See [[mods.surround]] for a more customizable version.
    * */
  def logged[F[_]: MessageLogger: FlatMap, H[_]](label: String)(
    implicit showH: Show[H[Health]]): HealthCheckEndoMod[F, H] = {

    mods.surround(MessageLogger[F].debug(show"Checking health for $label")) { result =>
      MessageLogger[F].debug(show"Health result for $label: ${result.value}")
    }
  }
}
