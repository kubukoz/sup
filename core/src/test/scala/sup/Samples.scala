package sup

import cats.data.{NonEmptyList, OneAnd}
import sup.data.Tagged

object Samples {
  import cats.effect._
  import cats._
  import cats.implicits._

  type TaggedNel[A] = NonEmptyList[Tagged[String, A]]

  val webCheck: HealthCheck[IO, Tagged[String, ?]] = new HealthCheck[IO, Tagged[String, ?]] {
    val check: IO[HealthResult[Tagged[String, ?]]] = IO.pure(HealthResult.tagged("WEB", Health.healthy))
  }

  def kafkaQueueCheck(queue: String): HealthCheck[IO, Id] = new HealthCheck[IO, Id] {
    val check: IO[HealthResult[Id]] = IO(println(s"Checking queue $queue")).as(HealthResult.one(Health.healthy))
  }

  val kafkaCheck = (kafkaQueueCheck("q1") |+| kafkaQueueCheck("q2")).mapK(Tagged.wrapK("kafka"))

  val reporter: HealthReporter[IO, TaggedNel] =
    HealthReporter.fromChecks(webCheck, kafkaCheck)

  val checked: IO[HealthResult[OneAnd[TaggedNel, ?]]] = reporter.check

//  val raw: IO[(Health, NonEmptyList[(String, Health)])] = reporter.check.map(_.value match {
//    case oa =>
//      (oa.head, oa.tail.map { tagged => tagged.tag -> tagged.health
//      })
//  })
}
