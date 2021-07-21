package sup

import cats.effect.ConcurrentEffect
import cats.effect.Timer
import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import cats.effect.implicits._
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.ContextShift
import cats.effect.IO
import scala.concurrent.ExecutionContext

trait BaseIOTest extends AnyWordSpec with Matchers {
  def ioTimeout: FiniteDuration = 5.seconds
  def runIO[F[_]: ConcurrentEffect: Timer](io: F[Assertion]): Assertion = io.timeout(ioTimeout).toIO.unsafeRunSync()

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
}
