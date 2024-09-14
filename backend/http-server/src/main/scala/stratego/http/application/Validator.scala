package stratego.http.application

import stratego.http.application.Error.ValidationError

import zio.json._
import zio.http.Request
import zio.{ZIO, Task}

object Validator {

  def validate[O](req: Request)(implicit decoder: JsonDecoder[O]): Task[O] = 
    for {
      body <- req.body.asString
      output  <- ZIO.fromEither(body.fromJson[O].left.map(s => ValidationError(req.url.encode, s)))
    } yield output  
}
