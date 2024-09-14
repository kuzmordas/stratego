package stratego.domain

sealed trait Player

object Player {
  case object Green extends Player
  case object Red extends Player
}
