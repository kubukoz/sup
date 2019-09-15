package sup
import cats.kernel.laws.discipline.EqTests
import cats.kernel.laws.discipline.MonoidTests
import cats.tagless.laws.discipline.FunctorKTests
import cats.tests.CatsSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.magnolia._

import scala.util.Try

import CatsTaglessInstances._

class HealthCheckCatsInstancesTests extends CatsSuite {
  implicit def arbitraryHealthCheck[F[_], H[_]](implicit A: Arbitrary[F[HealthResult[H]]]): Arbitrary[HealthCheck[F, H]] =
    Arbitrary(A.arbitrary.map(HealthCheck.liftF))

  implicit def cogenHealthCheck[F[_], H[_]](implicit C: Cogen[F[HealthResult[H]]]): Cogen[HealthCheck[F, H]] =
    C.contramap(_.check)

  checkAll("FunctorK[HealthCheck[Option, ?[_]]", FunctorKTests[HealthCheck[Option, ?[_]]].functorK[Try, Option, List, Int])
  checkAll("Monoid[HealthCheck[Try, Option]]", MonoidTests[HealthCheck[Try, Option]].monoid)
  checkAll("Eq[HealthCheck[Try, Option]]", EqTests[HealthCheck[Try, Option]].eqv)
}
