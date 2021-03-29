package sup

import _root_.doobie.Transactor
import cats.effect.Async
import cats.effect.IO
import scala.concurrent.duration._
import cats.implicits._
import scala.concurrent.ExecutionContext
import cats.effect.Temporal

class DoobieCheckSpec extends BaseIOTest {

  def goodTransactor[F[_]: Async: ContextShift]: Transactor[F] =
    Transactor.fromDriverManager[F]("org.h2.Driver", "jdbc:h2:mem:")

  def badTransactor[F[_]: Async: ContextShift]: Transactor[F] =
    Transactor.fromDriverManager[F]("org.h2.Driver", "jdbcfoobarnope")

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Temporal[IO] = IO.timer(ExecutionContext.global)

  "IO H2 check" when {
    "the database responds before the timeout" should {
      "be Healthy" in runIO {
        val healthCheck = modules.doobie.connectionCheck(goodTransactor[IO])(timeout = 5.seconds.some)

        healthCheck.check.map {
          _.value shouldBe Health.Healthy
        }
      }
    }

    "there is no timeout" should {
      "be Healthy" in runIO {
        val healthCheck = modules.doobie.connectionCheck(goodTransactor[IO])(timeout = none)

        healthCheck.check.map {
          _.value shouldBe Health.Healthy
        }
      }
    }
  }
}
