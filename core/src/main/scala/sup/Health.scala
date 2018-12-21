package sup
import cats.Eq

sealed trait Health extends Product with Serializable {

  def isGood: Boolean = this match {
    case Health.Good => true
    case Health.Bad  => false
  }
}

object Health {
  case object Good extends Health
  case object Bad extends Health

  val good: Health = Good
  val bad: Health = Bad

  val inverse: Health => Health = {
    case Good => Bad
    case Bad => Good
  }

  implicit val eq: Eq[Health] = Eq.fromUniversalEquals
}
