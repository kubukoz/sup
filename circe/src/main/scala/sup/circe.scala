package sup

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object circe {
  implicit val healthEncoder: Encoder[Health] = deriveEncoder
  implicit val healthDecoder: Decoder[Health] = deriveDecoder

  implicit def healthResultEncoder[H[_]](implicit E: Encoder[H[Health]]): Encoder[HealthResult[H]] =
    E.contramap(_.value)

  implicit def healthResultDecoder[H[_]](implicit D: Decoder[H[Health]]): Decoder[HealthResult[H]] =
    D.map(HealthResult(_))
}
