package stratego.http.application

import zio.json._
import stratego.http.application.Responses._
import stratego.http.application.data.domain._
import stratego.domain.Field
import java.util.UUID
import stratego.domain.GameState.Move
import stratego.http.domain.User
import stratego.domain.Entity
import stratego.domain.Player

object Requests {

  case class CreateSessionRequest(user: User.WithPlayer)
  object CreateSessionRequest {
    implicit val decoder: JsonDecoder[CreateSessionRequest] = DeriveJsonDecoder.gen[CreateSessionRequest]
    implicit val encoder: JsonEncoder[CreateSessionRequest] = DeriveJsonEncoder.gen[CreateSessionRequest]
  }

  case class GetSessionById(sessionId: UUID, userId: Long)
  object GetSessionById {
    implicit val decoder: JsonDecoder[GetSessionById] = DeriveJsonDecoder.gen[GetSessionById]
    implicit val encoder: JsonEncoder[GetSessionById] = DeriveJsonEncoder.gen[GetSessionById]
  }

  case class JoinToSessionRequest(sessionId: UUID, user: User.WithoutPlayer)
  object JoinToSessionRequest {
    implicit val decoder: JsonDecoder[JoinToSessionRequest] = DeriveJsonDecoder.gen[JoinToSessionRequest]
    implicit val encoder: JsonEncoder[JoinToSessionRequest] = DeriveJsonEncoder.gen[JoinToSessionRequest]
  }

  case class UpdateSessionRequest(sessionId: UUID, move: Move, userId: Long)
  object UpdateSessionRequest {
    implicit val decoder: JsonDecoder[UpdateSessionRequest] = DeriveJsonDecoder.gen[UpdateSessionRequest]
    implicit val encoder: JsonEncoder[UpdateSessionRequest] = DeriveJsonEncoder.gen[UpdateSessionRequest]
  }

  case class GetMovementsRequest(sessionId: UUID, entity: Entity)
  object GetMovementsRequest {
    implicit val decoder: JsonDecoder[GetMovementsRequest] = DeriveJsonDecoder.gen[GetMovementsRequest]
    implicit val encoder: JsonEncoder[GetMovementsRequest] = DeriveJsonEncoder.gen[GetMovementsRequest]
  }

}
