package sup

import cats.kernel.CommutativeMonoid
import cats.{Eq, Show}
import cats.implicits._

/**
  * The component's health status. It can only be Healthy or Sick - there's no middle ground (no Unknown state).
  * */
sealed trait Health extends Product with Serializable {

  def isHealthy: Boolean = this match {
    case Health.Healthy => true
    case Health.Sick    => false
  }
}

object Health {
  case object Healthy extends Health
  case object Sick    extends Health

  val healthy: Health = Healthy
  val sick: Health    = Sick

  val fromBoolean: Boolean => Health = {
    case true  => Healthy
    case false => Sick
  }

  val fromString: String => Option[Health] = {
    case "Healthy" => Healthy.some
    case "Sick"    => Sick.some
    case _         => None
  }

  implicit val eq: Eq[Health] = Eq.fromUniversalEquals

  /**
    * A monoid that'll return [[Sick]] if any of the combined values are sick, [[Healthy]] otherwise.
    * */
  implicit val allHealthyCommutativeMonoid: CommutativeMonoid[Health] = new CommutativeMonoid[Health] {
    override val empty: Health                         = healthy
    override def combine(x: Health, y: Health): Health = if (x.isHealthy) y else sick
  }

  implicit val show: Show[Health] = Show.fromToString
}
