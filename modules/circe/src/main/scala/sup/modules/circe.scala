package sup.modules

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import sup.data.{Report, Tagged}
import sup.{Health, HealthResult}

object circe {
  implicit val healthCirceEncoder: Encoder[Health] = Encoder[String].contramap(_.toString)
  implicit val healthCirceDecoder: Decoder[Health] =
    Decoder[String].emap(s => Health.fromString(s).toRight(s"$s is not a valid ${Health.getClass.getName}"))

  implicit def taggedCirceEncoder[Tag: Encoder, H: Encoder]: Encoder[Tagged[Tag, H]] = deriveEncoder
  implicit def taggedCirceDecoder[Tag: Decoder, H: Decoder]: Decoder[Tagged[Tag, H]] = deriveDecoder

  implicit def reportCirceEncoder[H[_], A: Encoder](implicit H: Encoder[H[A]]): Encoder[Report[H, A]] =
    deriveEncoder

  implicit def reportCirceDecoder[H[_], A: Decoder](implicit H: Decoder[H[A]]): Decoder[Report[H, A]] =
    deriveDecoder

  implicit def healthResultCirceEncoder[H[_]](implicit E: Encoder[H[Health]]): Encoder[HealthResult[H]] =
    E.contramap(_.value)

  implicit def healthResultCirceDecoder[H[_]](implicit D: Decoder[H[Health]]): Decoder[HealthResult[H]] =
    D.map(HealthResult(_))
}
