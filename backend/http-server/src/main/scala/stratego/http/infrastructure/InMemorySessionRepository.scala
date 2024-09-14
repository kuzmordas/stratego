package stratego.http.infrastructure

import stratego.http.domain._
import java.util.UUID
import stratego.domain.GameState
import zio.{Task, Ref, ZLayer}
import zio.ZIO
import java.time.Instant
import zio.Duration

object InMemorySessionRepository {
    val live = ZLayer.fromZIO(
    for {
      map <- Ref.Synchronized.make(Map.empty[UUID, Session])
    } yield InMemorySessionRepository(map)
  )
}

final case class InMemorySessionRepository(mapRef: Ref.Synchronized[Map[UUID,Session]]) extends SessionRepository {

  override def add(session: Session): Task[Unit] = 
    for {
      _ <- mapRef.update(m => m + ((session.id, session)))
    } yield ()

  override def filterOld(ttl: Duration): Task[List[Session]] =
    for {
      now <- zio.Clock.instant
      res <- mapRef.get.map(_.filter {
        case (_, session) => session.updatedAt match {
          case Some(updatedAt) => updatedAt.plusSeconds(ttl.toSeconds()).isBefore(now) 
          case None => session.createdAt.plusSeconds(ttl.toSeconds()).isBefore(now)
        }
      })
    } yield res.map(_._2).toList

  override def find(id: UUID): Task[Option[Session]] =
    for {
      res <- mapRef.get.map(_.get(id))
    } yield res

  override def findInactive(id: UUID): Task[Option[Session.Inactive]] =
    for {
      res <- mapRef.get.map(_.get(id))
    } yield res.flatMap {
      case s: Session.Inactive => Some(s)
      case _ => None
    }

  override def findActive(id: UUID): Task[Option[Session.Active]] =
    for {
      res <- mapRef.get.map(_.get(id))
    } yield res.flatMap {
      case s: Session.Active => Some(s)
      case _ => None
    }

  override def replaceInactive(active: Session.Active): Task[Unit] =
    for {
      _ <- mapRef.update(m => m + ((active.id, active)))
    } yield ()

  override def updateActive(id: UUID, f: Session.Active => Task[Session.Active]): Task[Option[Session.Active]] =
    for {
      optRes <- mapRef.modifyZIO(m => m.get(id) match {
        case Some(session: Session.Active) => f(session).map(res => (Some(res), m + ((session.id, res))))
        case _ => ZIO.succeed((None, m))
      })
    } yield optRes

  override def remove(id: UUID): Task[Unit] =
    for {
      _ <- mapRef.update(m => m - id)
    } yield ()

}
