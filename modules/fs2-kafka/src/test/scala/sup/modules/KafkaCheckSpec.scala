package sup.modules

import cats.effect.{IO, Resource}
import com.dimafeng.testcontainers.{ForEachTestContainer, KafkaContainer}
import fs2.kafka.{AdminClientSettings, KafkaAdminClient}
import org.apache.kafka.clients.admin.NewTopic
import sup.BaseIOTest
import sup.modules.kafka.{clusterCheck, topicsCheck}

import java.util.Optional
import scala.concurrent.duration._

class KafkaCheckSpec extends BaseIOTest with ForEachTestContainer {

  private val kafkaTimeout: FiniteDuration = 2.seconds

  override val container: KafkaContainer = KafkaContainer()
  override def ioTimeout: FiniteDuration = 30.seconds

  "Kafka health checker" when {
    "kafka cluster is up and running" should {
      "return an healthy result" in runIO {
        createClient.use(client => clusterCheck[IO](client, kafkaTimeout).check.map(h => assert(h.value.isHealthy)))
      }
    }

    "kafka cluster is down" should {
      "return an healthy result" in runIO {
        createClient.use { client =>
          IO(container.stop()) *> clusterCheck[IO](client, kafkaTimeout).check.map(h => assert(!h.value.isHealthy))
        }
      }
    }

    "kafka topics exists" should {
      "return an healthy result" in runIO {
        createClient.use { client =>
          val topic1 = new NewTopic("topic-1", Optional.empty[Integer](), Optional.empty[java.lang.Short]())
          val topic2 = new NewTopic("topic-2", Optional.empty[Integer](), Optional.empty[java.lang.Short]())

          client.createTopics(List(topic1, topic2)) *> topicsCheck[IO](
            client,
            kafkaTimeout
          )(topic1.name(), topic2.name()).check.map(h => assert(h.value.isHealthy))
        }
      }
    }

    "kafka topics does not exists" should {
      "return an unhealthy result" in runIO {
        createClient.use { client =>
          topicsCheck[IO](client, kafkaTimeout)("topic-0").check.map(h => assert(!h.value.isHealthy))
        }
      }
    }
  }

  private def createClient: Resource[IO, KafkaAdminClient[IO]] = {
    val settings = AdminClientSettings(container.bootstrapServers)
    KafkaAdminClient.resource(settings)
  }
}
