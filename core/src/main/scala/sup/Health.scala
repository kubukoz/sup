package sup

sealed trait Health extends Product with Serializable {

  def isGood: Boolean = this match {
    case Health.Good => true
    case Health.Bad  => false
  }
}

object Health {
  case object Good extends Health
  case object Bad extends Health
}
