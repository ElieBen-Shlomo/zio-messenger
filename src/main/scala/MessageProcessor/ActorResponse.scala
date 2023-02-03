package MessageProcessor

import DB.MessageRepo
import Model.ConsoleStrings._
import Model.State.State
import Model.{LoginDetails, UserSession}
import Services.MessageServiceDB
import Services.StateServiceJvm.{addUser, findUnpairedUser, findUserToMessage, removeUser}
import zio.http.Channel
import zio.http.socket.WebSocketFrame
import zio.{Task, ZIO}

object ActorResponse {

  def sessionJoined(
      loginDetails: LoginDetails,
      ch: Channel[WebSocketFrame]
  )(state: State): ZIO[Any, Throwable, (State, Unit)] = {
    val unpairedUserSession = findUnpairedUser(state, loginDetails.userId)
    unpairedUserSession match {
      case None                   => noPair(loginDetails, ch)(state)
      case Some(otherUserSession) => foundPair(otherUserSession, loginDetails, ch)(state)
    }
  }

  def sessionLeft(loginDetails: LoginDetails)(state: State): ZIO[Any, Throwable, (State, Unit)] = {
    val userToMessage = findUserToMessage(state, loginDetails.userId)
    for {
      _ <- userToMessage match {
        case None => ZIO.succeed({})
        case Some(otherUser) =>
          websocketWrite(otherUser.channel, disconnected(loginDetails.username))
      }
      newState <- removeUser(state, loginDetails.userId)
    } yield (newState, {})
  }

  def messageSent(
      loginDetails: LoginDetails,
      ch: Channel[WebSocketFrame],
      msg: String
  )(state: State): ZIO[MessageRepo.DataService, Throwable, (State, Unit)] = {
    val userToMessage = findUserToMessage(state, loginDetails.userId)
    for {
      _ <- userToMessage match {
        case None => websocketWrite(ch, noPairYet)
        case Some(otherUser) =>
          MessageServiceDB.writeChatMessage(msg, loginDetails.userId, otherUser.userId) *>
            websocketWrite(ch, s"${loginDetails.username.username}: $msg") *>
            websocketWrite(otherUser.channel, s"${loginDetails.username.username}: $msg")
      }
    } yield (state, {})
  }

  private def noPair(
      loginDetails: LoginDetails,
      ch: Channel[WebSocketFrame]
  )(state: State): ZIO[Any, Throwable, (State, Unit)] = for {
    newState <- addUser(
      state,
      UserSession(
        loginDetails.userId,
        loginDetails.username,
        pairedUser = None,
        channel = ch
      )
    )
    _ <- websocketWrite(ch, waiting(loginDetails.username))
  } yield (newState, {})

  private def foundPair(
      otherUserSession: UserSession,
      loginDetails: LoginDetails,
      ch: Channel[WebSocketFrame]
  )(state: State): ZIO[Any, Throwable, (State, Unit)] = {
    val thisUserSession = UserSession(
      loginDetails.userId,
      loginDetails.username,
      pairedUser = Some(otherUserSession.userId),
      channel = ch
    )
    for {
      newState <- addUser(state, thisUserSession)
      newState <- addUser(
        newState,
        otherUserSession.copy(pairedUser = Some(thisUserSession.userId))
      )
      _ <- websocketWrite(ch, connected(otherUserSession.username))
      _ <- websocketWrite(
        otherUserSession.channel,
        connected(loginDetails.username)
      )
    } yield (newState, {})
  }

  private def websocketWrite(ch: Channel[WebSocketFrame], msg: String): Task[Unit] =
    ch.writeAndFlush(WebSocketFrame.text(msg))
}
