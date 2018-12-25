package sup.dupa.yolo.swag
import sup.data.Report

object foo {
  import sup._
  import sup.modules.http4s._, sup.modules.http4sclient._, sup.modules.circe._

  import org.http4s.circe._, org.http4s._, org.http4s.client._, cats.effect._, cats._, cats.data._

  implicit val client: Client[IO] = Client.fromHttpApp(HttpApp.notFound[IO])

  implicit val encoder: EntityEncoder[IO, HealthResult[Id]] = jsonEncoderOf[IO, HealthResult[Id]]

  val healthcheck: HealthCheck[IO, Id] = statusCodeHealthCheck(Request[IO]())

  val routes = healthCheckRoutes(healthcheck)

  //also works with reports!
  implicit val reportEntityEncoder: EntityEncoder[IO, HealthResult[Report[NonEmptyList, ?]]] =
    jsonEncoderOf

  val report = HealthReporter.fromChecks(
    healthcheck,
    HealthCheck.const[IO, Id](Health.Sick)
  )

  val reportRoutes = healthCheckRoutes(report)
}
