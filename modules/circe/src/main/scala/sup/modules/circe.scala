package sup.modules

import io.circe.Decoder
import io.circe.Encoder
import sup.data.Report
import sup.data.Tagged
import sup.Health
import sup.HealthResult

object circe {
  implicit val healthCirceEncoder: Encoder[Health] = Encoder[String].contramap(_.toString)

  implicit val healthCirceDecoder: Decoder[Health] =
    Decoder[String].emap(s => Health.fromString(s).toRight(s"$s is not a valid ${Health.getClass.getName}"))

  implicit def taggedCirceEncoder[Tag: Encoder, H: Encoder]: Encoder[Tagged[Tag, H]] =
    Encoder.forProduct2("tag", "health")(tagged => (tagged.tag, tagged.health))

  implicit def taggedCirceDecoder[Tag: Decoder, H: Decoder]: Decoder[Tagged[Tag, H]] =
    Decoder.forProduct2("tag", "health")(Tagged.apply)

  implicit def reportCirceEncoder[G[_], H[_], A: Encoder](implicit H: Encoder[G[H[A]]]): Encoder[Report[G, H, A]] =
    Encoder.forProduct2("health", "checks")(report => (report.health, report.checks))

  implicit def reportCirceDecoder[G[_], H[_], A: Decoder](implicit H: Decoder[G[H[A]]]): Decoder[Report[G, H, A]] =
    Decoder.forProduct2("health", "checks")(Report.apply)

  implicit def healthResultCirceEncoder[H[_]](implicit E: Encoder[H[Health]]): Encoder[HealthResult[H]] =
    E.contramap(_.value)

  implicit def healthResultCirceDecoder[H[_]](implicit D: Decoder[H[Health]]): Decoder[HealthResult[H]] =
    D.map(HealthResult(_))
}
