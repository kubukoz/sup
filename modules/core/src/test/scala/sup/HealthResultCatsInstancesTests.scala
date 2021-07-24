package sup

import cats.kernel.laws.discipline.{EqTests, MonoidTests}
import cats.tests.CatsSuite

class HealthResultCatsInstancesTests extends CatsSuite {
  import ScalacheckInstances._

  checkAll("Monoid[HealthResult[Option]]", MonoidTests[HealthResult[Option]].monoid)
  checkAll("Eq[HealthResult[Option]]", EqTests[HealthResult[Option]].eqv)
}
