package sup
import cats.{Eq, Monoid}

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

  implicit val eq: Eq[Health] = Eq.fromUniversalEquals

  val allHealthyMonoid: Monoid[Health] = new Monoid[Health] {
    override val empty: Health                         = sick
    override def combine(x: Health, y: Health): Health = if (x.isHealthy) y else sick
  }
}
