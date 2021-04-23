package sup

import cats.implicits._
import dev.profunktor.redis4cats.algebra.Ping
import sup.modules.redis
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class RedisCheckSpec extends AnyWordSpec with Matchers {
  "Either check" when {
    "Right" should {
      "be Healthy" in {
        implicit val ping: Ping[Either[String, *]] = new Ping[Either[String, *]] {
          override val ping: Either[String, String] = Right("pong")
          override def select(index: Int): Either[String, Unit] = Left("Not implemented")
        }

        val healthCheck = redis.pingCheck

        healthCheck.check shouldBe Right(HealthResult.one(Health.Healthy))
      }
    }

    "Left" should {
      "be Sick" in {
        implicit val ping: Ping[Either[String, *]] = new Ping[Either[String, *]] {
          override val ping: Either[String, String] = Left("boo")
          override def select(index: Int): Either[String, Unit] = Left("Not implemented")
        }

        val healthCheck = modules.redis.pingCheck

        healthCheck.check shouldBe Right(HealthResult.one(Health.Sick))
      }
    }
  }
}
