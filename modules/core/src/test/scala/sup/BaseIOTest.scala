package sup

import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.IO
import cats.effect.unsafe.IORuntime

trait BaseIOTest extends AnyWordSpec with Matchers {
  def ioTimeout: FiniteDuration = 5.seconds
  def runIO(io: IO[Assertion]): Assertion = io.timeout(ioTimeout).unsafeRunSync()(IORuntime.global)
}
