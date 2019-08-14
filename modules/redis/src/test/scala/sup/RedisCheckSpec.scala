package sup

import cats.implicits._
import dev.profunktor.redis4cats.algebra.Ping
import org.scalatest.{Matchers, WordSpec}
import sup.modules.redis

class RedisCheckSpec extends WordSpec with Matchers {
  "Either check" when {
    "Right" should {
      "be Healthy" in {
        implicit val ping: Ping[Either[String, ?]] = new Ping[Either[String, ?]] {
          override val ping: Either[String, String] = Right("pong")
        }

        val healthCheck = redis.pingCheck

        healthCheck.check shouldBe Right(HealthResult.one(Health.Healthy))
      }
    }

    "Left" should {
      "be Sick" in {
        implicit val ping: Ping[Either[String, ?]] = new Ping[Either[String, ?]] {
          override val ping: Either[String, String] = Left("boo")
        }

        val healthCheck = modules.redis.pingCheck

        healthCheck.check shouldBe Right(HealthResult.one(Health.Sick))
      }
    }
  }
}
