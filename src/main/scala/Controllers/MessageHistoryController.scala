package Controllers

import Model.Username.Username
import Services.{Message, MessageServiceDB}
import zio.http._
import zio.json.EncoderOps

object MessageHistoryController {

  def getMessages(thisUsername: Username, otherUsername: Username) = for {
    messages <- MessageServiceDB.getMessages(thisUsername, otherUsername)
    (sent, received) = messages
  } yield {
    val chatHistory = combineMessages(sent, received)
    Response.json(chatHistory.toJson)
  }

  private def combineMessages(sent: List[Message], received: List[Message]) = {
    (sent ++ received) // too lazy to do this DB side
      .sortBy(_.timestamp)
      .reverse
      .map(msg => s"${msg.username}: ${msg.msg.replace("\n", "")}")
  }
}
