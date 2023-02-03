package Server

import DB.{MessageRepo, UserRepo}
import Model.WebsocketEventType
import zio.actors.ActorRef
import zio.http._
import zio.{Console, ZIO, ZLayer}

import java.io.IOException

object WebServer {

  private val port = 9000

  val configLayer: ZLayer[Any, Nothing, ServerConfig] = ServerConfig
    .live(ServerConfig.default.port(port))

  def startServer(
      messageProcessor: ActorRef[WebsocketEventType]
  ): ZIO[UserRepo.DataService with MessageRepo.DataService with Server, IOException, Nothing] =
    Console.printLine(s"Starting server on http://localhost:$port") *>
      Server.serve(app(messageProcessor))

  private def app(
      messageProcessor: ActorRef[WebsocketEventType]
  ): HttpApp[UserRepo.DataService with MessageRepo.DataService with Server, Throwable] =
    Routes.login ++ Routes.joinChatSession(messageProcessor) ++ Routes.getMessages

}
