package Server

import Controllers.{LoginController, MessageHistoryController, WebSocketController}
import DB.{MessageRepo, UserRepo}
import Model.UserId.UserId
import Model.Username.Username
import Model.{LoginDetails, WebsocketEventType}
import zio.ZIO
import zio.actors.ActorRef
import zio.http._
import zio.http.model.Method
import zio.json.DecoderOps

object Routes {

  def login: HttpApp[UserRepo.DataService with MessageRepo.DataService, Throwable] =
    Http.collectZIO[Request] { case req @ Method.POST -> !! / "login" =>
      LoginController.login(req)
    }

  def joinChatSession(
      messageProcessor: ActorRef[WebsocketEventType]
  ): HttpApp[UserRepo.DataService with MessageRepo.DataService, Throwable] =
    Http.collectZIO[Request] { case req @ Method.GET -> !! / "chat-session" =>
      WebSocketController.connect(req, messageProcessor)
    }

  def getMessages: HttpApp[UserRepo.DataService with MessageRepo.DataService, Throwable] =
    Http.collectZIO[Request] {
      case _ @ Method.GET -> !! / "messages" / thisUsername / otherUsername =>
        MessageHistoryController.getMessages(Username(thisUsername), Username(otherUsername))
    }
}
