package sup

import cats.implicits._
import com.github.gvolpe.fs2redis.algebra.Ping
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
