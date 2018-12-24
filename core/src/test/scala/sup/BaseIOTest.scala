package sup
import cats.effect.{ConcurrentEffect, Timer}
import org.scalatest.compatible.Assertion
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._
import cats.effect.implicits._

trait BaseIOTest extends WordSpec with Matchers {
  def ioTimeout: FiniteDuration                                         = 5.seconds
  def runIO[F[_]: ConcurrentEffect: Timer](io: F[Assertion]): Assertion = io.timeout(ioTimeout).toIO.unsafeRunSync()
}
