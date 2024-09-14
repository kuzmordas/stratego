package stratego.http

import zio.ZIOAppDefault
import zio.{Scope, Runtime, ZIO, ZIOAppArgs}
import zio.http.Server
import stratego.http.application.AppRoutes
import stratego.http.infrastructure._
import zio.ZLayer
import stratego.domain.Field.Point
import stratego.domain.GameState
import stratego.domain.Player
import stratego.domain.Field
import stratego.domain.Entity.Soldier
import tofu.logging.zlogs.TofuZLogger
import zio.config.typesafe.TypesafeConfigProvider
import stratego.http.domain.SessionCleaner

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs,Any,Any] =
    Runtime.removeDefaultLoggers ++
    TofuZLogger.addToRuntime ++ Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath().kebabCase)

  private val serverConfig = ZLayer.fromZIO {
    ZIO.service[Config].map(config => 
      Server.Config.default
        .port(config.httpServer.port)
        .disableRequestStreaming(config.httpServer.contentLength)
    )
  }

  override def run =
    (for {
      config <- ZIO.service[Config]
      _ <- SessionCleaner.run(config.session).fork
      port <- Server.install(AppRoutes.all)
      _ <- ZIO.logInfo(s"Stratego game server starter on $port")
      _ <- ZIO.never
    } yield ())
      .provide(
        Config.live,
        serverConfig,
        Server.live,
        InMemorySessionRepository.live
      )
      .tapError(e => ZIO.logError(e.getMessage()))


    
  
}
