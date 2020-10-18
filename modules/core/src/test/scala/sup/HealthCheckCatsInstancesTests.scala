package sup

import cats.kernel.laws.discipline.EqTests
import cats.kernel.laws.discipline.MonoidTests
import cats.tagless.laws.discipline.FunctorKTests
import cats.tests.CatsSuite

import scala.util.Try

import CatsTaglessInstances._

class HealthCheckCatsInstancesTests extends CatsSuite {
  import ScalacheckInstances._

  checkAll(
    "FunctorK[HealthCheck[Option, *[_]]",
    FunctorKTests[HealthCheck[Option, *[_]]].functorK[Try, Option, List, Int]
  )
  checkAll("Monoid[HealthCheck[Try, Option]]", MonoidTests[HealthCheck[Try, Option]].monoid)
  checkAll("Eq[HealthCheck[Try, Option]]", EqTests[HealthCheck[Try, Option]].eqv)
}
