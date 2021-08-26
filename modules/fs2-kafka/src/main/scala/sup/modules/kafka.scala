package sup.modules

import cats.Id
import cats.effect.kernel.Temporal
import fs2.kafka.KafkaAdminClient
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException
import cats.implicits._
import sup.{mods, Health, HealthCheck, HealthResult}
import cats._

import scala.concurrent.duration.FiniteDuration

object kafka {

  def clusterCheck[F[_]: Temporal](client: KafkaAdminClient[F], timeout: FiniteDuration) =
    HealthCheck
      .liftF {
        client.describeCluster.clusterId.as(HealthResult.one(Health.healthy))
      }
      .through(mods.timeoutToSick(timeout))

  def topicsCheck[F[_]: Temporal](
    client: KafkaAdminClient[F],
    timeout: FiniteDuration
  )(
    topicName: String*
  ): HealthCheck[F, Id] =
    HealthCheck
      .liftFBoolean {
        client.describeTopics(topicName.toList).map(_.keySet.exists(topicName.contains)).recover {
          case _: UnknownTopicOrPartitionException => false
        }
      }
      .through(mods.timeoutToSick(timeout))

}
