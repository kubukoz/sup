package sup

import cats.data.NonEmptyList

case class HealthReport(checks: NonEmptyList[Health], status: Health)
