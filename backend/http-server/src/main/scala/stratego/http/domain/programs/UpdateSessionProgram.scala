package stratego.http.domain.programs

import zio.{ZIO, RIO}
import java.util.UUID
import stratego.domain.GameState
import stratego.http.domain.SessionRepository
import stratego.http.domain.Session
import stratego.http.domain.Error.SessionNotFound
import stratego.domain.Player.Green
import stratego.domain.Player.Red
import stratego.domain.Player

object UpdateSessionProgram {
  def run(sessionId: UUID, move: GameState.Move, userId: Long): RIO[SessionRepository, Session.WithHiddenState] = 
    for {
      _ <- ZIO.logDebug(s"UpdateSession started | sessionId: $sessionId, move: $move")
      nextSession <- SessionRepository
        .updateActive(
          sessionId,
          (session: Session.Active) => Session.Active.update(session, move)
        )
        .someOrFail(SessionNotFound(sessionId))
      hiddenPlayer <- ZIO.fromEither(nextSession.getHiddenPlayer(userId))
      _ <- nextSession.state.winner match {
        case None => ZIO.unit
        case Some(winner) =>
          val user = if (nextSession.firstUser.player == winner) nextSession.firstUser.player
          else nextSession.secondUser.player
          ZIO.logInfo(s"Seession has a winner | sessionId: ${nextSession.id}, winner: $user")
      }
      _ <- ZIO.logDebug(s"UpdateSession finished | nextSession: $nextSession")
    } yield Session.WithHiddenState.fromSession(nextSession, hiddenPlayer)
}
