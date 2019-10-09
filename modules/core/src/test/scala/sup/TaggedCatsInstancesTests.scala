package sup

import cats.kernel.laws.discipline.EqTests
import cats.laws.discipline._
import cats.tests.CatsSuite
import sup.data.Tagged

class TaggedCatsInstancesTests extends CatsSuite {
  import ScalacheckInstances._

  checkAll("Reducible[Tagged[String, ?]]", ReducibleTests[Tagged[String, ?]].reducible[List, Int, Int])
  checkAll("Eq[Tagged[String, Int]]", EqTests[Tagged[String, Int]].eqv)
}
