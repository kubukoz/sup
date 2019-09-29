package sup

import cats.kernel.laws.discipline.CommutativeMonoidTests
import cats.kernel.laws.discipline.EqTests
import cats.tests.CatsSuite
import org.scalacheck.magnolia._

class HealthCatsInstancesTests extends CatsSuite {
  import ScalacheckInstances._

  checkAll("Eq[Health]", EqTests[Health].eqv)
  checkAll("CommutativeMonoid[Health]", CommutativeMonoidTests[Health].commutativeMonoid)
}
