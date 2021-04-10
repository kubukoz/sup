package sup

import cats.effect.ConcurrentEffect
import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import cats.effect.implicits._
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.Temporal

trait BaseIOTest extends AnyWordSpec with Matchers {
  def ioTimeout: FiniteDuration = 5.seconds
  def runIO[F[_]: ConcurrentEffect: Temporal](io: F[Assertion]): Assertion = io.timeout(ioTimeout).toIO.unsafeRunSync()
}
