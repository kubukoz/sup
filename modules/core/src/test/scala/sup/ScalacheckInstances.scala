package sup

import org.scalacheck.Cogen
import org.scalacheck.Arbitrary
import sup.data.Tagged

object ScalacheckInstances {
  implicit val cogenHealth: Cogen[Health] = Cogen.cogenBoolean.contramap(_.isHealthy)

  implicit def arbitraryHealthCheck[F[_], H[_]](implicit A: Arbitrary[F[HealthResult[H]]]): Arbitrary[HealthCheck[F, H]] =
    Arbitrary(A.arbitrary.map(HealthCheck.liftF))

  implicit def cogenHealthResult[H[_]](implicit C: Cogen[H[Health]]): Cogen[HealthResult[H]] = C.contramap(_.value)
  implicit def cogenHealthCheck[F[_], H[_]](implicit C: Cogen[F[HealthResult[H]]]): Cogen[HealthCheck[F, H]] =
    C.contramap(_.check)

  implicit def cogenTagged[Tag: Cogen, H: Cogen]: Cogen[Tagged[Tag, H]] = Cogen[(Tag, H)].contramap { tagged =>
    (tagged.tag, tagged.health)
  }
}
