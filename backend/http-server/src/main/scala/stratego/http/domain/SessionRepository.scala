package stratego.http.domain

import zio.{ZIO, Task, RIO}
import java.util.UUID
import java.time.Instant
import zio.Duration

trait SessionRepository {
  def add(session: Session): Task[Unit]
  def find(id: UUID): Task[Option[Session]]
  def filterOld(ttl: Duration): Task[List[Session]]
  def findInactive(id: UUID): Task[Option[Session.Inactive]]
  def findActive(id: UUID): Task[Option[Session.Active]]
  def replaceInactive(active: Session.Active): Task[Unit]
  def updateActive(id: UUID, f: Session.Active => Task[Session.Active]): Task[Option[Session.Active]]
  def remove(id: UUID): Task[Unit]
}

object SessionRepository {
  def add(session: Session): RIO[SessionRepository, Unit] =
    ZIO.serviceWithZIO[SessionRepository](_.add(session))

  def filterOld(ttl: Duration): RIO[SessionRepository, List[Session]] =
    ZIO.serviceWithZIO[SessionRepository](_.filterOld(ttl))

  def find(id: UUID): RIO[SessionRepository, Option[Session]] =
    ZIO.serviceWithZIO[SessionRepository](_.find(id))
  
  def findInactive(id: UUID): RIO[SessionRepository, Option[Session.Inactive]] =
    ZIO.serviceWithZIO[SessionRepository](_.findInactive(id))

  def findActive(id: UUID): RIO[SessionRepository, Option[Session.Active]] =
    ZIO.serviceWithZIO[SessionRepository](_.findActive(id))

  def replaceInactive(active: Session.Active): RIO[SessionRepository, Unit] =
    ZIO.serviceWithZIO[SessionRepository](_.replaceInactive(active))

  def updateActive(id: UUID, f: Session.Active => Task[Session.Active]): RIO[SessionRepository, Option[Session.Active]] =
    ZIO.serviceWithZIO[SessionRepository](_.updateActive(id, f))

  def remove(id: UUID): RIO[SessionRepository, Unit] =
    ZIO.serviceWithZIO[SessionRepository](_.remove(id))    
}
