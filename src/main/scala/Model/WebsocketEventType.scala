package Model

import zio.http.Channel
import zio.http.socket.WebSocketFrame

sealed trait WebsocketEventType[+_]

case class SessionJoined(
    loginDetails: LoginDetails,
    channel: Channel[WebSocketFrame]
) extends WebsocketEventType[Unit]

case class SessionLeft(loginDetails: LoginDetails) extends WebsocketEventType[Unit]

case class MessageSent(loginDetails: LoginDetails, msg: String, channel: Channel[WebSocketFrame])
    extends WebsocketEventType[Unit]
