package stratego.http.domain

import stratego.domain.Player
import stratego.domain.Player.Green
import stratego.domain.Player.Red

sealed trait User {
  val id: Long
  val fio: String
  val userName: Option[String]
  val avatar: Option[String]
}

object User {

  final case class WithPlayer(
    id: Long,
    fio: String,
    userName: Option[String],
    avatar: Option[String],
    player: Player
  ) extends User

  final case class WithoutPlayer(
    id: Long,
    fio: String,
    userName: Option[String],
    avatar: Option[String],
  ) extends User {

    def withPlayer(firstUser: WithPlayer) = 
      firstUser.player match {
        case Green => WithPlayer(id, fio, userName, avatar, Red)
        case Red => WithPlayer(id, fio, userName, avatar, Green)
      }
  }


}
