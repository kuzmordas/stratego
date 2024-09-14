package stratego.http.application

import stratego.http.domain.Session
import stratego.http.domain.programs._
import stratego.http.application.Validator.validate
import stratego.http.application.data.domain._
import stratego.http.application.Error.ValidationError
import stratego.http.application.Requests._
import stratego.http.application.Responses._

import zio.{ZIO, Task}
import zio.json._
import zio.http.{Routes, Method, handler, Response, Request, Root}
import java.util.UUID
import scala.util.Try

object AppRoutes {

  val all =
    Routes(
      Method.ANY / "healthcheck" -> handler(Response.text("Ok")),
      Method.POST / "create-session" -> handler { (req: Request) =>
        for {
          data <- validate[CreateSessionRequest](req)
          result <- CreateSessionProgram.run(data.user)
        } yield Response.json(result.toJson)
      },
      Method.POST / "get-session-by-id" -> handler { (req: Request) =>
        for {
          data <- validate[GetSessionById](req)
          result <- GetActiveSessionProgram.run(data.sessionId, data.userId)
        } yield Response.json(result.toJson)
      },
      Method.POST / "join-to-session" -> handler { (req: Request) =>
        for {
          data <- validate[JoinToSessionRequest](req)
          result <- JoinToSessionProgram.run(data.sessionId, data.user)
        } yield Response.json(result.fold(_.toJson, _.toJson))
      },
      Method.POST / "move" -> handler { (req: Request) => 
        for {
          data <- validate[UpdateSessionRequest](req)
          result <- UpdateSessionProgram.run(data.sessionId, data.move, data.userId)
        } yield Response.json(result.toJson)
      },
      Method.POST / "get-movements" -> handler { (req: Request) => 
        for {
          data <- validate[GetMovementsRequest](req)
          result <- GetMovementsProgram.run(data.sessionId, data.entity)
        } yield Response.json(result.toJson)
      }
    )
    .mapErrorZIO(error => Logging.logAndMapError(error)) @@ Logging.requestLoggingAspect()

}
