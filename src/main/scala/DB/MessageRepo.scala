package DB

import DB.UserRepo.Users
import io.getquill._
import io.getquill.jdbczio.Quill
import zio.{ZIO, ZLayer}

import java.time.Instant

object MessageRepo {

  case class Messages(
      id: Long,
      message: String,
      senderUserId: Long,
      receiverUserId: Long,
      timestamp: Instant
  )

  case class QueryService(quill: Quill.Postgres[SnakeCase]) {

    import quill._

    def insert = quote { (msg: Messages) =>
      query[Messages].insertValue(msg).returningGenerated(_.id)
    }

    def get = quote { (thisUsername: String, thatUsername: String) =>
      for {
        thisUser <- query[Users].filter(_.username == thisUsername).take(1)
        thatUser <- query[Users].filter(_.username == thatUsername).take(1)
        message  <- query[Messages]
        if message.senderUserId == thisUser.id && message.receiverUserId == thatUser.id
      } yield message
    }

  }

  object QueryService {
    def live: ZLayer[Quill.Postgres[SnakeCase], Nothing, QueryService] =
      ZLayer.fromFunction(QueryService(_))
  }

  case class DataService(queryService: QueryService) {

    import queryService.quill._

    def insert(msg: Messages) =
      run(queryService.insert(lift(msg)))

    def get(thisUsername: String, thatUsername: String) =
      run(queryService.get(lift(thisUsername), lift(thatUsername)))

  }

  object DataService {
    def insert(msg: Messages) =
      ZIO.serviceWithZIO[DataService](_.insert(msg))

    def get(thisUsername: String, thatUsername: String) =
      ZIO.serviceWithZIO[DataService](_.get(thisUsername, thatUsername))

    def live: ZLayer[QueryService, Nothing, DataService] =
      ZLayer.fromFunction(DataService(_))
  }

}
