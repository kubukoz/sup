package sup

import cats.{~>, Eq}
import cats.data.{NonEmptyList, OneAnd}
import cats.kernel.laws.discipline.MonoidTests
import cats.tests.CatsSuite
import org.scalacheck.Arbitrary
import sup.HealthReporter.HealthReporter

import scala.util.Try

class HealthCheckModInstancesTest extends CatsSuite {
//
//  import org.scalacheck.ScalacheckShapeless._
//
//   implicit def funEq[A, B: Eq](implicit FA: Arbitrary[A], EqFb: Eq[B]): Eq[A => B] = Eq.instance[A => B] { (f1, f2) =>
//     Try {
//       check {
//         org.scalacheck.Prop.forAll(FA.arbitrary)(input => EqFb.eqv(f1(input), f2(input)))
//       }
//     }.isSuccess
//   }
//
//  implicit def modArb[F[_]](implicit arb: Arbitrary[F[Health] => F[Health]]): Arbitrary[HealthCheckMod[F]] =
//    Arbitrary(arb.arbitrary.map(new HealthCheckMod[F](_)))
//
//  implicit def modEq[F[_]](implicit eqf: Eq[F[Health] => F[Health]]): Eq[HealthCheckMod[F]] = Eq.by(_.modify)
//
//  checkAll("HealthCheckMod.MonoidLaws[Option]", MonoidTests[HealthCheckMod[Option]].monoid)
}

object Samples {
  import cats.effect._
  import cats.implicits._
  import cats.effect.implicits._
  import cats._
  import alg.FunctorK.ops._

  type TaggedNel[A] = NonEmptyList[Tagged[String, A]]

  val webCheck: HealthCheck[IO, Tagged[String, ?]] = new HealthCheck[IO, Tagged[String, ?]] {
    val check: IO[HealthResult[Tagged[String, ?]]] = IO.pure(HealthResult.tagged("WEB", Health.good))
  }

  val kafkaCheck: HealthCheck[IO, Tagged[String, ?]] = new HealthCheck[IO, Id] {
    val check: IO[HealthResult[Id]] = IO.pure(HealthResult.one(Health.good))
  }.mapK(Tagged.tagK("KAFKA"))

  val reporter: HealthReporter[IO, TaggedNel] = HealthReporter.fromChecks(webCheck, kafkaCheck)

  val checked: IO[HealthResult[OneAnd[TaggedNel, ?]]] = reporter.check

  val raw: IO[(Health, NonEmptyList[(String, Health)])] = reporter.check.map(_.value match {
    case OneAnd(h, t) =>
      (h, t.map { tagged =>
        tagged.tag -> tagged.health
      })
  })
}
