package stratego.http.application

object Error {

  sealed trait ApplicationError
  case class ValidationError(
    path: String,
    details: String,
    message: String = "Request not valid",
  ) extends Throwable with ApplicationError
  
  
}
