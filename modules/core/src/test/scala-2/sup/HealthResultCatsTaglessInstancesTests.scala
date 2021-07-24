package sup

import cats.tagless.laws.discipline.FunctorKTests
import cats.tests.CatsSuite
import sup.CatsTaglessInstances._

import scala.util.Try

class HealthResultCatsTaglessInstancesTests extends CatsSuite {
  import ScalacheckInstances._

  checkAll("FunctorK[HealthResult]", FunctorKTests[HealthResult].functorK[Try, Option, List, Int])
}
