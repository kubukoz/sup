package sup

import cats.data.NonEmptyList

final case class HealthReport(checks: NonEmptyList[Health], status: Health)
