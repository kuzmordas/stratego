package stratego.http.domain.programs

import zio.{ZIO, RIO}
import stratego.http.domain._
import stratego.http.domain.Error.SessionNotFound
import stratego.domain._
import java.util.UUID


object GetMovementsProgram {
  def run(sessionId: UUID, entity: Entity): RIO[SessionRepository, List[Field.Point]] = 
    for {
      _ <- ZIO.logDebug(s"GetMovementsProgram started | sessionId: $sessionId, entity: $entity")
      session <- SessionRepository.findActive(sessionId).someOrFail(SessionNotFound(sessionId))
      res = session.state.getMovements(entity)
      _ <- ZIO.logDebug(s"GetMovementsProgram finished | movements: $res")
    } yield res
}
