package sup

import cats.effect.IO
import cats.effect.Resource
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.CqlSessionBuilder
import com.dimafeng.testcontainers.CassandraContainer
import com.dimafeng.testcontainers.ForAllTestContainer

import java.net.InetSocketAddress
import scala.concurrent.duration._

class CassandraCheckSpec extends BaseIOTest with ForAllTestContainer {
  override val container: CassandraContainer = CassandraContainer()

  override def ioTimeout: FiniteDuration = 30.seconds

  "Cassandra health checker" when {
    "cassandra is up and running" should {
      "return an healthy result" in runIO {
        createSession.use { ses =>
          cassandra.connectionCheck[IO](ses).check.map(h => assert(h.value.isHealthy))
        }
      }
    }

    "cassandra is not available" should {
      "return an unhealthy result" in runIO {
        createSession.use { session =>
          IO(container.stop()) *>
            cassandra.connectionCheck[IO](session).through(mods.recoverToSick).check.map { h =>
              assert(!h.value.isHealthy)
            }
        }
      }
    }
  }

  private def createSession: Resource[IO, CqlSession] =
    Resource.make {
      IO(
        new CqlSessionBuilder()
          .withLocalDatacenter("datacenter1")
          .addContactPoint(InetSocketAddress.createUnresolved(container.host, container.container.getFirstMappedPort))
          .build()
      )
    } { ses =>
      IO.fromCompletableFuture(IO(ses.closeAsync().toCompletableFuture)).void
    }

}
