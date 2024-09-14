package stratego.http.domain.programs

import java.util.UUID
import zio.{ZIO, RIO}
import stratego.http.domain._
import stratego.http.domain.Error._
import stratego.domain.Player._
import stratego.http.domain.Session._

object JoinToSessionProgram {
  def run(sessionId: UUID, user: User.WithoutPlayer): RIO[SessionRepository, Either[Session.Inactive, Session.WithHiddenState]] =
    for {
      _ <- ZIO.logDebug(s"JoinToSessionProgram started | sessionId: $sessionId, user: $user")
      session <- SessionRepository.find(sessionId).someOrFail(SessionNotFound(sessionId))
      result <- session match {
        case active: Active => 
          for {
            session <- ZIO.fromEither(active.isUserBelongToSession(user).map(_ => active))
            hiddenPlayer <- ZIO.fromEither(active.getHiddenPlayer(user.id))
            hidden = Session.WithHiddenState.fromSession(active, hiddenPlayer)
          } yield Right(hidden)
        case inactive: Inactive =>
          for {
            activeOrFail <- Session.Active.make(inactive, user)
            result <- activeOrFail match {
              case Left(e: WrongSecondPlayerInSession) => ZIO.fail(e)
              case Left(_) => ZIO.succeed(Left(inactive))
              case Right(active) =>
                for {
                  _ <- SessionRepository.replaceInactive(active)
                  // todo: you can get hidden player when you create session or check active
                  hiddenPlayer <- ZIO.fromEither(active.getHiddenPlayer(user.id))
                  hidden = Session.WithHiddenState.fromSession(active, hiddenPlayer)
                } yield Right(hidden)
            }
          } yield result
      }
      _ <- result match {
        case Left(inactive) => 
          ZIO.logInfo(s"Host successfully joined to session | joinedUser: ${inactive.firstUser}, sessionId: ${inactive.id}")
        case Right(active) =>
          ZIO.logInfo(s"Second player successfully joined to session | firstUser: ${active.firstUser}, secondUser: ${active.secondUser}, sessionId: ${active.id}")
      }
    } yield result
}

