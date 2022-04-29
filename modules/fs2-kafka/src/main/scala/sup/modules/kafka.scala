package sup.modules

import cats._
import cats.syntax.all._
import fs2.kafka.KafkaAdminClient
import org.apache.kafka.common.errors.{TimeoutException, UnknownTopicOrPartitionException}
import sup.{Health, HealthCheck, HealthResult}

object kafka {

  def clusterCheck[F[_]: MonadThrow](client: KafkaAdminClient[F]): HealthCheck[F, Id] =
    HealthCheck.liftF {
      client.describeCluster.clusterId.as(HealthResult.one(Health.healthy)).recover { case _: TimeoutException =>
        HealthResult.one(Health.sick)
      }
    }

  def topicsCheck[F[_]: MonadThrow](client: KafkaAdminClient[F])(topicNames: String*): HealthCheck[F, Id] =
    HealthCheck.liftFBoolean {
      client.describeTopics(topicNames.toList).map(result => topicNames.forall(result.keySet.contains)).recover {
        case _: UnknownTopicOrPartitionException => false
      }
    }

}
