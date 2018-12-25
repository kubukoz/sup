package sup

import cats.kernel.laws.discipline.EqTests
import cats.laws.discipline._
import cats.tests.CatsSuite
import sup.data.Tagged
import org.scalacheck.ScalacheckShapeless._

class TaggedCatsInstancesTests extends CatsSuite {
  checkAll("Foldable[Tagged[String, ?]]", FoldableTests[Tagged[String, ?]].foldable[Int, Int])
  checkAll("Eq[Tagged[String, Int]]", EqTests[Tagged[String, Int]].eqv)
}
