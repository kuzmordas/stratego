package stratego.http.domain

import stratego.domain.GameState
import java.util.UUID
import java.time.Instant
import zio.{ZIO, Task}
import zio.Clock
import stratego.domain.Player
import stratego.http.domain.Error._

sealed trait Session {
  val id: UUID
  val createdAt: Instant
  val updatedAt: Option[Instant]
}

object Session {

  case class Inactive(id: UUID,
                      firstUser: User.WithPlayer,
                      createdAt: Instant,
                      updatedAt: Option[Instant]) extends Session

  object Inactive {
    def make(firstUser: User.WithPlayer): Task[Inactive] =
      for {
        now <- Clock.instant
        id = UUID.randomUUID()
      } yield Inactive(id, firstUser, now, None)

  }

  case class Active(id: UUID,
                    firstUser: User.WithPlayer,
                    secondUser: User.WithPlayer,
                    state: GameState,
                    events: List[GameState.Event],
                    createdAt: Instant,
                    updatedAt: Option[Instant]) extends Session {
    def isUserBelongToSession(user: User): Either[UserNotBelongToSession, Unit] = 
      if (user.id == firstUser.id || user.id == secondUser.id)
        Right(())
      else 
        Left(UserNotBelongToSession(id, user.id)) 

    def getHiddenPlayer(userId: Long): Either[UserNotBelongToSession, Player] = 
      if (firstUser.id == userId) firstUser.player match {
        case Player.Green => Right(Player.Red)
        case Player.Red => Right(Player.Green)
      }
      else if (secondUser.id == userId) secondUser.player match {
        case Player.Green => Right(Player.Red)
        case Player.Red => Right(Player.Green)
      } else Left(UserNotBelongToSession(id, userId))
  }

  object Active {
    def make(inactiveSession: Inactive, secondUser: User.WithoutPlayer): Task[Either[WrongUser, Active]] =
      if (inactiveSession.firstUser.id == secondUser.id)
        ZIO.succeed(Left(WrongSecondUserInSession(inactiveSession.id)))
      else for {
        now <- Clock.instant
        state = GameState.make(10, 10, None)
      } yield
        Right(
          Active(
            inactiveSession.id,
            inactiveSession.firstUser,
            secondUser.withPlayer(inactiveSession.firstUser),
            GameState.make(10, 10, None),
            List.empty,
            inactiveSession.createdAt,
            Some(now)
          )
        )

    def update(session: Active, move: GameState.Move): Task[Session.Active] =
      for {
        now <- Clock.instant
        _ <- ZIO.when(now.isBefore(session.updatedAt.getOrElse(session.createdAt))) {
          ZIO.fail(SessionAlreadyUpdated(session.id))
        }
        result = session.state.execute(move)
        newSession = result.event match {
          case GameState.Nothing => session
          case event => session.copy(
            state = result.state,
            events = session.events :+ event,
            updatedAt = Some(now)
          )
        }
      } yield newSession
  }


  case class WithHiddenState(
    id: UUID,
    firstUser: User.WithPlayer,
    secondUser: User.WithPlayer,
    state: GameState.Hidden,
    events: List[GameState.Event],
    createdAt: Instant,
    updatedAt: Option[Instant]
  )

  object WithHiddenState {

    def fromSession(session: Active, hiddenPlayer: Player): WithHiddenState =
      WithHiddenState(
        session.id,
        session.firstUser,
        session.secondUser,
        GameState.Hidden.fromState(session.state, hiddenPlayer),
        session.events,
        session.createdAt,
        session.updatedAt
      )

  }
  
}
