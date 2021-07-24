package sup

import cats.kernel.laws.discipline.{EqTests, MonoidTests}
import cats.tests.CatsSuite
import sup.CatsTaglessInstances._

import scala.util.Try

class HealthCheckCatsInstancesTests extends CatsSuite {
  import ScalacheckInstances._

  checkAll("Monoid[HealthCheck[Try, Option]]", MonoidTests[HealthCheck[Try, Option]].monoid)
  checkAll("Eq[HealthCheck[Try, Option]]", EqTests[HealthCheck[Try, Option]].eqv)
}
