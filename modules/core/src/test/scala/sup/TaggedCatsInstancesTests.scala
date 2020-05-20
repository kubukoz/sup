package sup

import cats.kernel.laws.discipline.EqTests
import cats.laws.discipline._
import cats.tests.CatsSuite
import sup.data.Tagged
import sup.data.TaggedT
import org.scalacheck.Arbitrary
import cats.kernel.Eq
import org.scalacheck.Gen

class TaggedCatsInstancesTests extends CatsSuite {
  import ScalacheckInstances._

  // checkAll("Reducible[Tagged[String, ?]]", ReducibleTests[Tagged[String, ?]].reducible[List, Int, Int])
  // checkAll("Eq[Tagged[String, Int]]", EqTests[Tagged[String, Int]].eqv)

  implicit def arbitraryTaggedT[E: Arbitrary, A: Arbitrary]: Arbitrary[TaggedT[E, A]] =
    Arbitrary {
      Gen.oneOf(
        for {
          e <- Arbitrary.arbitrary[E]
          r <- Arbitrary.arbitrary[A]
        } yield TaggedT.Incorrect(e, r),
        for {
          r <- Arbitrary.arbitrary[A]
        } yield TaggedT.Correct(r)
      )
    }

  implicit def eqTaggedT[E: Eq, A: Eq]: Eq[TaggedT[E, A]] = Eq.by {
    case TaggedT.Incorrect(e, v) => Left((e, v))
    case TaggedT.Correct(v)      => Right(v)
  }

  checkAll("Reducible[TaggedT[String, ?]]", ReducibleTests[TaggedT[String, ?]].reducible[List, Int, Int])
}
