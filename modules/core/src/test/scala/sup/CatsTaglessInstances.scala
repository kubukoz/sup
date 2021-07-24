package sup

import cats.Eq
import cats.~>
import cats.arrow.FunctionK
import org.scalacheck.{Arbitrary, Gen}

import scala.util.Try

object CatsTaglessInstances {

  implicit val catsDataArbitraryOptionList: Arbitrary[FunctionK[Option, List]] = Arbitrary(
    Gen.const(new ~>[Option, List] {
      def apply[A](fa: Option[A]): List[A] = fa.toList
    })
  )

  implicit val catsDataArbitraryListOption: Arbitrary[FunctionK[List, Option]] = Arbitrary(
    Gen.const(new ~>[List, Option] {
      def apply[A](fa: List[A]): Option[A] = fa.headOption
    })
  )

  implicit val catsDataArbitraryTryOption: Arbitrary[FunctionK[Try, Option]] = Arbitrary(
    Gen.const(new ~>[Try, Option] {
      def apply[A](fa: Try[A]): Option[A] = fa.toOption
    })
  )

  implicit val catsDataArbitraryOptionTry: Arbitrary[FunctionK[Option, Try]] = Arbitrary(
    Gen.const(new ~>[Option, Try] {
      def apply[A](fa: Option[A]): Try[A] = Try(fa.get)
    })
  )

  implicit val catsDataArbitraryListVector: Arbitrary[FunctionK[List, Vector]] = Arbitrary(
    Gen.const(new ~>[List, Vector] {
      def apply[A](fa: List[A]): Vector[A] = fa.toVector
    })
  )

  implicit val catsDataArbitraryVectorList: Arbitrary[FunctionK[Vector, List]] = Arbitrary(
    Gen.const(new ~>[Vector, List] {
      def apply[A](fa: Vector[A]): List[A] = fa.toList
    })
  )

  implicit val eqThrow: Eq[Throwable] = Eq.fromUniversalEquals
}
