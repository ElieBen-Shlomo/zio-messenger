package MessageProcessor

import DB.MessageRepo
import Model.State.State
import Model.UserId.UserId
import Model._
import zio.ZIO
import zio.actors.Actor.Stateful
import zio.actors.{ActorRef, ActorSystem, Context, Supervisor}

object Actor {

  def messageProcessor: ZIO[MessageRepo.DataService, Throwable, ActorRef[WebsocketEventType]] =
    for {
      system <- ActorSystem("zio-messenger")
      processor <- system.make(
        "processor-actor",
        Supervisor.none,
        Map.empty[UserId, UserSession],
        actor
      )
    } yield processor

  private def actor: Stateful[MessageRepo.DataService, State, WebsocketEventType] =
    new Stateful[MessageRepo.DataService, State, WebsocketEventType] {
      override def receive[A](
          state: State,
          wsEvent: WebsocketEventType[A],
          context: Context
      ): ZIO[MessageRepo.DataService, Throwable, (State, A)] = {
        wsEvent match {
          case SessionJoined(loginDetails, ch) =>
            ActorResponse.sessionJoined(loginDetails, ch)(state)

          case SessionLeft(loginDetails) =>
            ActorResponse.sessionLeft(loginDetails)(state)

          case MessageSent(loginDetails, msg, ch) =>
            ActorResponse.messageSent(loginDetails, ch, msg)(state)
        }
      }
    }

}
