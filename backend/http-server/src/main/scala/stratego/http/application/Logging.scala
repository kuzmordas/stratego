package stratego.http.application

import tofu.logging.zlogs.ZLogAnnotation
import zio.http.HandlerAspect
import zio.http.{Handler, Request, Response}
import zio.http.Status._
import zio.{ZIO, UIO, Trace}
import stratego.http.domain.{Error => DomainError}
import stratego.http.application.Error.ValidationError
import stratego.http.application.Responses.toResponse
import tofu.syntax.error

object Logging {

  private val notLoging = Set(
    (200, "/get-session-by-id"),
    (200, "/get-movements")
  )
  
  private val statusCodeAnnotation: ZLogAnnotation[Int] = ZLogAnnotation.make("statusCode")
  private val methodAnnitation: ZLogAnnotation[String] = ZLogAnnotation.make("method")
  private val pathAnnitation: ZLogAnnotation[String] = ZLogAnnotation.make("path")
  private val durationAnnitation: ZLogAnnotation[String] = ZLogAnnotation.make("duration")
  private val stackTraceAnnitation: ZLogAnnotation[String] = ZLogAnnotation.make("stackTrace")

  def requestLoggingAspect()(implicit trace: Trace): HandlerAspect[Any, Unit] = 
    HandlerAspect.interceptHandlerStateful(Handler.fromFunctionZIO[Request] { request =>
      zio.Clock.instant.map(now => ((now, request), (request, ())))
    })( 
      Handler.fromFunctionZIO[((java.time.Instant, Request), Response)] {
      case ((start, request), response) => 
        for {
          end <- zio.Clock.instant
          
          duration = java.time.Duration.between(start, end)
          reqAndStatus = (response.status.code, request.url.path.toString())
          _ <- ZIO.when(!notLoging.contains(reqAndStatus)) {
              ZIO.log("request serverd") @@
                statusCodeAnnotation(response.status.code) @@
                methodAnnitation(request.method.toString()) @@
                pathAnnitation(request.url.encode) @@
                durationAnnitation(s"${duration.toMillis()} ms")
          }  
        } yield response
  })

  def logAndMapError(e: Throwable): UIO[Response] = 
    e match {
      case e: ValidationError =>
        (ZIO.logError(s"request not valid: ${e.details}") @@ pathAnnitation(e.path)).map(_ => toResponse(e))
      case e: DomainError =>
        ZIO.logError(s"Domain error: ${e.message} | error id: ${e.id}, error code: ${e.code}").map(_ => toResponse(e.asInstanceOf[DomainError]))
      case e =>
        val stackTrace = e.getStackTrace().map(_.toString()).mkString(",")
        (ZIO.logError(s"unexpected error: ${e.getMessage()}") @@ stackTraceAnnitation(stackTrace)).map(_ => toResponse(e))
    }

}
