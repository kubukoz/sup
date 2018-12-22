package sup
import cats.{Eq, Monoid}

sealed trait Health extends Product with Serializable {

  def isGood: Boolean = this match {
    case Health.Good => true
    case Health.Bad  => false
  }
}

object Health {
  case object Good extends Health
  case object Bad  extends Health

  val good: Health = Good
  val bad: Health  = Bad

  implicit val eq: Eq[Health] = Eq.fromUniversalEquals

  val allGoodMonoid: Monoid[Health] = new Monoid[Health] {
    override val empty: Health                         = bad
    override def combine(x: Health, y: Health): Health = if (x.isGood) y else bad
  }
}
