package stratego.http.domain

import zio.ZIO
import zio.Schedule
import zio.Duration
import stratego.http.Config


object SessionCleaner {
  
  def run(config: Config.Session) =
    ZIO.logInfo(s"Start cleaning process in background | config: $config") *>
      clean(config.ttl).schedule(Schedule.spaced(config.interval))

  def clean(ttl: zio.Duration) =
    for {
      sessionsToDie <- SessionRepository.filterOld(ttl)
      _ <- ZIO.foreach(sessionsToDie)(session =>
        for {
          _ <- SessionRepository.remove(session.id)
          _ <- ZIO.logInfo(s"The session was deleted because it was inactive for too long | sessionId: ${session.id}")
        } yield ()
      )
    } yield ()

}
