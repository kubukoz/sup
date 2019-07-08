package sup

import _root_.doobie.Transactor
import cats.effect.{Async, ContextShift, IO, Timer}
import eu.timepit.refined.auto._

import scala.concurrent.ExecutionContext

class DoobieCheckSpec extends BaseIOTest {

  def goodTransactor[F[_]: Async: ContextShift]: Transactor[F] =
    Transactor.fromDriverManager[F]("org.h2.Driver", "jdbc:h2:mem:")

  def badTransactor[F[_]: Async: ContextShift]: Transactor[F] =
    Transactor.fromDriverManager[F]("org.h2.Driver", "jdbcfoobarnope")

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO]     = IO.timer(ExecutionContext.global)

  "IO H2 check" when {
    "the database responds before the timeout" should {
      "be Healthy" in runIO {
        val healthCheck = modules.doobie.connectionCheck(goodTransactor[IO])(timeoutSeconds = Some(5))

        healthCheck.check.map {
          _.value shouldBe Health.Healthy
        }
      }
    }

    "there is no timeout" should {
      "be Healthy" in runIO {
        val healthCheck = modules.doobie.connectionCheck(goodTransactor[IO])(timeoutSeconds = None)

        healthCheck.check.map {
          _.value shouldBe Health.Healthy
        }
      }
    }
  }
}
