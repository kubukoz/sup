package sup

import cats.effect.{Blocker, ContextShift, IO}
import com.datastax.oss.driver.api.core.{CqlSession, CqlSessionBuilder}
import com.dimafeng.testcontainers.{CassandraContainer, Container, ForAllTestContainer}
import org.scalatest.flatspec.AnyFlatSpec

import java.net.InetSocketAddress
import scala.concurrent.ExecutionContext

class CassandraCheckSpec extends AnyFlatSpec with ForAllTestContainer {
  override val container: CassandraContainer = CassandraContainer()
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val blocker: Blocker = Blocker.liftExecutionContext(ExecutionContext.global)

  "Cassandra health checker" should "return an healthy result when cassandra is up and running" in {
    assert(cassandra.connectionCheck[IO](createSession.unsafeRunSync()).check.unsafeRunSync().value.isHealthy)
  }


  it should "return an unhealthy result when cassandra is not available" in {
    val session = createSession.unsafeRunSync()
    container.stop()
    assert(!cassandra.connectionCheck[IO](session).through(mods.recoverToSick).check.unsafeRunSync().value.isHealthy)
  }

  private def createSession: IO[CqlSession] = {
    IO(new CqlSessionBuilder()
      .withLocalDatacenter("datacenter1")
      .addContactPoint(InetSocketAddress.createUnresolved("localhost", container.container.getFirstMappedPort))
      .build())
  }

}
