package sup

import cats.kernel.laws.discipline.{CommutativeGroupTests, CommutativeMonoidTests, EqTests}
import cats.laws.discipline._
import cats.tests.CatsSuite
import sup.data.Tagged
import org.scalacheck.ScalacheckShapeless._

class HealthCatsInstancesTests extends CatsSuite {
  checkAll("Eq[Health]", EqTests[Health].eqv)
  checkAll("CommutativeMonoid[Health]", CommutativeMonoidTests[Health].commutativeMonoid)
}
