package Model

import Model.UserId.UserId
import Model.Username.Username
import zio.http.Channel
import zio.http.socket.WebSocketFrame

case class UserSession(
    userId: UserId,
    username: Username,
    pairedUser: Option[UserId],
    channel: Channel[WebSocketFrame]
)
