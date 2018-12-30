package sup

import cats.kernel.laws.discipline.EqTests
import cats.laws.discipline._
import cats.tests.CatsSuite
import sup.data.Tagged
import org.scalacheck.ScalacheckShapeless._

class TaggedCatsInstancesTests extends CatsSuite {
  checkAll("Reducible[Tagged[String, ?]]", ReducibleTests[Tagged[String, ?]].reducible[List, Int, Int])
  checkAll("Eq[Tagged[String, Int]]", EqTests[Tagged[String, Int]].eqv)
}
