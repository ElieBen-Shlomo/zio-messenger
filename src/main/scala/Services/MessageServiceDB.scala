package Services

import DB.MessageRepo
import DB.MessageRepo.Messages
import Model.UserId.UserId
import Model.Username.Username

import java.time.Instant

case class Message(username: Username, timestamp: Instant, msg: String)
object Message {
  def fromDB(username: Username, messageDB: Messages) = Message(
    username,
    messageDB.timestamp,
    messageDB.message
  )
}

object MessageServiceDB {

  def writeChatMessage(msg: String, senderId: UserId, receiverId: UserId) = {
    val autoIncrementId = 0L
    val message = Messages(
      autoIncrementId,
      msg,
      senderId.id,
      receiverId.id,
      Instant.now()
    )
    MessageRepo.DataService.insert(message)
  }

  def getMessages(thisUsername: Username, otherUsername: Username) = {
    for {
      sentPar     <- MessageRepo.DataService.get(thisUsername.username, otherUsername.username).fork
      receivedPar <- MessageRepo.DataService.get(otherUsername.username, thisUsername.username).fork
      sent        <- sentPar.join
      received    <- receivedPar.join
    } yield (
      sent.map(Message.fromDB(thisUsername, _)),
      received.map(Message.fromDB(otherUsername, _))
    )
  }

}
