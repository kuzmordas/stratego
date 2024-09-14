package stratego.domain

sealed trait Entity {
  val id: Int
  val player: Player
}

object Entity {
  case class Flag(id: Int, player: Player) extends Entity
  case class Mine(id: Int, player: Player) extends Entity

  sealed trait Unit extends Entity {
    val force: Int
  }

  case class Marhsal(id: Int, player: Player, force: Int = 1) extends Unit
  case class General(id: Int, player: Player, force: Int = 2) extends Unit
  case class Colonel(id: Int, player: Player, force: Int = 3) extends Unit
  case class Major(id: Int, player: Player, force: Int = 4) extends Unit
  case class Captain(id: Int, player: Player, force: Int = 5) extends Unit
  case class Lieutenant(id: Int, player: Player, force: Int = 6) extends Unit
  case class Sergeant(id: Int, player: Player, force: Int = 7) extends Unit
  case class Sapper(id: Int, player: Player, force: Int = 8) extends Unit
  case class Soldier(id: Int, player: Player, force: Int = 9) extends Unit
  case class Spy(id: Int, player: Player, force: Int = 10) extends Unit
}
