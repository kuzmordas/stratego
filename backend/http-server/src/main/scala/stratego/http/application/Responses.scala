package stratego.http.application

import zio.json._
import stratego.http.domain.{Error => DomainError}
import zio.http.{Response, Status}
import stratego.http.application.Error.ValidationError
import stratego.domain.Field
import stratego.domain.Entity
import stratego.domain.Entity._
import stratego.domain.Player
import stratego.domain.Player.Green
import stratego.domain.Player.Red
import stratego.domain.GameState
import stratego.http.domain.Session
import java.util.UUID

object Responses {

  case class RequestFailed(message: String, details: Option[String] = None, code: Option[Int] = None, id: Option[UUID] = None)
  object RequestFailed {
    implicit val decoder: JsonDecoder[RequestFailed] = DeriveJsonDecoder.gen[RequestFailed]
    implicit val encoder: JsonEncoder[RequestFailed] = DeriveJsonEncoder.gen[RequestFailed]
  }

  def toResponse(e: ValidationError): Response = 
    Response
      .json(RequestFailed(e.message, Some(e.details)).toJson)
      .status(Status.BadRequest)

  def toResponse(e: DomainError): Response = 
    Response
      .json(RequestFailed(e.message, code = Some(e.code), id = Some(e.id)).toJson)
      .status(Status.Conflict)

  def toResponse(e: Throwable): Response = 
    Response
      .json(RequestFailed("Unexpected error").toJson)
      .status(Status.InternalServerError) 

  // def toResponse(e: Throwable): Response = 
  //   e match {
  //     case e: ValidationError =>
  //       Response
  //         .json(RequestFailed(e.message, Some(e.details)).toJson)
  //         .status(Status.BadRequest)
  //     case e: DomainError =>
  //       Response
  //         .json(RequestFailed(e.message, code = Some(e.code)).toJson)
  //         .status(Status.Conflict)
  //     case e =>
  //       Response
  //         .json(RequestFailed("Unexpected error").toJson)
  //         .status(Status.InternalServerError)
  //   }
}
