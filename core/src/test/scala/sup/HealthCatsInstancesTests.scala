package sup

import cats.kernel.laws.discipline.{CommutativeMonoidTests, EqTests}
import cats.tests.CatsSuite
import org.scalacheck.ScalacheckShapeless._

class HealthCatsInstancesTests extends CatsSuite {
  checkAll("Eq[Health]", EqTests[Health].eqv)
  checkAll("CommutativeMonoid[Health]", CommutativeMonoidTests[Health].commutativeMonoid)
}
