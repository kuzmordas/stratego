package stratego.http

import zio.ZLayer
import zio.ZIO
import zio.config.magnolia.deriveConfig
import stratego.http.Config.HttpServer
import zio.Duration
import stratego.http.Config.Session

object Config {
  case class HttpServer(port: Int, contentLength: Int)
  case class Session(ttl: Duration, interval: Duration)

  val live = ZLayer.fromZIO(ZIO.config[Config](deriveConfig[Config]))
}

case class Config(httpServer: HttpServer, session: Session)
