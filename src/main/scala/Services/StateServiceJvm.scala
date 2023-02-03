package Services

import Model.State.State
import Model.UserId.UserId
import Model.UserSession
import zio.ZIO

object StateServiceJvm {

  def findUnpairedUser(
      state: State,
      thisUserId: UserId
  ): Option[UserSession] = state.find { case (userId, userSession) =>
    thisUserId != userId && userSession.pairedUser.isEmpty
  }.map(_._2)

  def findUserToMessage(state: State, userId: UserId): Option[UserSession] =
    for {
      userSession      <- state.get(userId)
      otherUser        <- userSession.pairedUser
      otherUserSession <- state.get(otherUser)
    } yield otherUserSession

  def addUser(
      state: State,
      userSession: UserSession
  ): ZIO[Any, Nothing, State] =
    ZIO.succeed(state + (userSession.userId -> userSession))

  def removeUser(
      state: State,
      thisUserId: UserId
  ): ZIO[Any, Nothing, State] = {
    val otherUserIdOpt = for {
      thisUser    <- state.get(thisUserId)
      otherUserId <- thisUser.pairedUser
    } yield otherUserId

    val finalState = otherUserIdOpt match {
      case None => state.removed(thisUserId)
      case Some(otherUserId) =>
        val newState = state.removed(thisUserId)

        val otherUserSessionOpt   = state.get(otherUserId)
        val updatedUserSessionOpt = otherUserSessionOpt.map(_.copy(pairedUser = None))

        updatedUserSessionOpt match {
          case None => newState
          case Some(updatedUserSession) =>
            newState.updated[UserSession](otherUserId, updatedUserSession)
        }
    }

    ZIO.succeed(finalState)
  }
}
