package sup

import cats.data.{NonEmptyList, OneAnd}
import cats.effect._
import sup.data.{HealthReporter, Tagged}

class Samples(implicit ConcurrentIO: Concurrent[IO], timer: Timer[IO]) {
  import cats._
  import cats.implicits._
  import scala.concurrent.duration._

  type TaggedNel[A] = NonEmptyList[Tagged[String, A]]

  val webCheck: HealthCheck[IO, Tagged[String, ?]] = new HealthCheck[IO, Tagged[String, ?]] {
    val check: IO[HealthResult[Tagged[String, ?]]] = IO.pure(HealthResult.tagged("WEB", Health.healthy))
  }

  def kafkaQueueCheck(queue: String): HealthCheck[IO, Id] = new HealthCheck[IO, Id] {
    val check: IO[HealthResult[Id]] = IO(println(s"Checking queue $queue")).as(HealthResult.one(Health.healthy))
  }

  val kafkaCheck = (kafkaQueueCheck("q1") |+| kafkaQueueCheck("q2").transform(mods.timeoutToSick(5.seconds))).mapK(mods.tagWith("kafka"))

  val reporter: HealthReporter[IO, TaggedNel] =
    HealthReporter.fromChecks(webCheck, kafkaCheck)

  val checked: IO[HealthResult[OneAnd[TaggedNel, ?]]] = reporter.check
}
