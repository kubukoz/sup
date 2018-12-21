package sup

import cats.Eq
import cats.kernel.laws.discipline.MonoidTests
import cats.tests.CatsSuite
import org.scalacheck.Arbitrary

import scala.util.Try

class HealthCheckModInstancesTest extends CatsSuite {

  import org.scalacheck.ScalacheckShapeless._

   implicit def fFunEq[A, B: Eq](implicit FA: Arbitrary[A], EqFb: Eq[B]): Eq[A => B] = Eq.instance[A => B] { (f1, f2) =>
     Try {
       check {
         org.scalacheck.Prop.forAll(FA.arbitrary)(input => EqFb.eqv(f1(input), f2(input)))
       }
     }.isSuccess
   }

  implicit def modArb[F[_]](implicit arb: Arbitrary[F[Health] => F[Health]]): Arbitrary[HealthCheckMod[F]] =
    Arbitrary(arb.arbitrary.map(new HealthCheckMod[F](_)))

  implicit def modEq[F[_]](implicit eqf: Eq[F[Health] => F[Health]]): Eq[HealthCheckMod[F]] = Eq.by(_.modify)

  checkAll("HealthCheckMod.MonoidLaws[Option]", MonoidTests[HealthCheckMod[Option]].monoid)
//  checkAll("HealthCheckMod.GroupLaws[Option]", GroupTests[HealthCheckMod[Option]].group)
}
