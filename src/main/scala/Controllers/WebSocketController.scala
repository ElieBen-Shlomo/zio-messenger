package Controllers

import DB.{MessageRepo, UserRepo}
import Model.{LoginDetails, MessageSent, SessionJoined, SessionLeft, WebsocketEventType}
import zio.ZIO
import zio.actors._
import zio.http.ChannelEvent.UserEvent.HandshakeComplete
import zio.http.ChannelEvent.{ChannelRead, ChannelUnregistered, UserEventTriggered}
import zio.http.socket.{WebSocketChannelEvent, WebSocketFrame}
import zio.http.{ChannelEvent, Http, Request, Response}
import zio.json._

object WebSocketController {
  def connect(
      req: Request,
      messageProcessor: ActorRef[WebsocketEventType]
  ): ZIO[UserRepo.DataService with MessageRepo.DataService, Exception, Response] = {
    for {
      loginDetails <- ZIO
        .fromEither(req.cookiesDecoded.head.content.fromJson[LoginDetails])
        .catchAll(str => ZIO.fail(new Exception(str)))
      response <- handleWebsocketEvent(messageProcessor, loginDetails).toSocketApp.toResponse
    } yield response
  }

  private def handleWebsocketEvent(
      processor: ActorRef[WebsocketEventType],
      loginDetails: LoginDetails
  ): Http[
    UserRepo.DataService with MessageRepo.DataService,
    Throwable,
    WebSocketChannelEvent,
    Unit
  ] = Http.collectZIO[WebSocketChannelEvent] {
    // Session Joined
    case ChannelEvent(ch, UserEventTriggered(event)) =>
      event match {
        case HandshakeComplete =>
          processor ! SessionJoined(loginDetails, ch)
        case _ => ZIO.unit
      }
    // Session Left
    case ChannelEvent(_, ChannelUnregistered) =>
      processor ! SessionLeft(loginDetails)
    // Message sent
    case ChannelEvent(ch, ChannelRead(WebSocketFrame.Text(msg))) =>
      processor ? MessageSent(loginDetails, msg, ch)
  }
}
