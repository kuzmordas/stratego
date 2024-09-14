package stratego.http.domain

import java.util.UUID

sealed trait Error {
  val id: UUID
  val code: Int
  val message: String
}

object Error {

  sealed trait WrongUser

  final case class WrongSecondUserInSession(sessionId: UUID, code: Int = 1) extends Throwable() with Error with WrongUser {
    val id = UUID.randomUUID()
    val message: String = s"Second user is the same as first user, for session with id: $sessionId"
  }

  final case class WrongSecondPlayerInSession(sessionId: UUID, code: Int = 2) extends Throwable() with Error with WrongUser {
    val id = UUID.randomUUID()
    val message: String = s"Second player is the same as first player, for session with id: $sessionId"
  }

  final case class UserNotBelongToSession(sessionId: UUID, userId: Long, code: Int = 3) extends Throwable() with Error {
    val id = UUID.randomUUID()
    val message: String = s"User with id: $userId not belong to session with id: $sessionId"
  }

  final case class InviteNotFound(inviteId: UUID, code: Int = 4) extends Throwable() with Error {
    val id = UUID.randomUUID()
    val message: String = s"Invite with id: $inviteId not found"
  }

  final case class InviteAlreadyAccepted(inviteId: UUID, code: Int = 5) extends Throwable() with Error {
    val id = UUID.randomUUID()
    val message: String = s"Invite wih id: $inviteId already accepted"
  }

  final case class SessionNotFound(sessionId: UUID, code: Int = 6) extends Throwable() with Error {
    val id = UUID.randomUUID()
    val message: String = s"Session wih id: $sessionId not found"
  }

  final case class SessionAlreadyUpdated(sessionId: UUID, code: Int = 7) extends Throwable() with Error {
    val id = UUID.randomUUID()
    val message: String = s"Session wih id: $sessionId already updated"
  }

}
