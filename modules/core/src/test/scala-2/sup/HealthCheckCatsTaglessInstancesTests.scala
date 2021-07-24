package sup

import cats.tagless.laws.discipline.FunctorKTests
import cats.tests.CatsSuite
import sup.CatsTaglessInstances._

import scala.util.Try

class HealthCheckCatsTaglessInstancesTests extends CatsSuite {
  import ScalacheckInstances._

  checkAll(
    "FunctorK[HealthCheck[Option, *[_]]",
    FunctorKTests[HealthCheck[Option, *[_]]].functorK[Try, Option, List, Int]
  )
}
