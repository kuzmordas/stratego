package stratego.http.domain.programs

import java.util.UUID
import stratego.http.domain.User
import zio.{ZIO, RIO}
import stratego.http.domain._
import stratego.http.domain.Error._
import stratego.domain.Player.Green
import stratego.domain.Player.Red

object CreateSessionProgram {
  def run(firstUser: User.WithPlayer): RIO[SessionRepository, Session.Inactive] =
    for {
      _ <- ZIO.logDebug(s"CreateSessionProgram started | firstUser: $firstUser")
      inactiveSession <- Session.Inactive.make(firstUser)
      _ <- SessionRepository.add(inactiveSession)
      _ <- ZIO.logInfo(s"Inactive session was created | firstUser: ${inactiveSession.firstUser}, sessionId: ${inactiveSession.id}")
    } yield inactiveSession
}
