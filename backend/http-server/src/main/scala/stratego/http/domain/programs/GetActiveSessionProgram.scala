package stratego.http.domain.programs

import java.util.UUID
import stratego.http.domain.User
import zio.{ZIO, RIO}
import stratego.http.domain._
import stratego.http.domain.Error._

object GetActiveSessionProgram {
  def run(sessionId: UUID, userId: Long): RIO[SessionRepository, Option[Session.WithHiddenState]] =
    for {
      _ <- ZIO.logDebug(s"GetSessionProgram started | sessionId: $sessionId")
      optActiveSession <- SessionRepository.findActive(sessionId)
      result <- optActiveSession match {
        case None => ZIO.succeed(None)
        case Some(session) => 
          ZIO.fromEither(
            session
              .getHiddenPlayer(userId)
              .map(hiddenPlayer => Session.WithHiddenState.fromSession(session, hiddenPlayer))
          ).map(Some(_))
      }
      _ <- ZIO.logDebug(s"GetSessionProgram finished | session: $result")
    } yield result
}
