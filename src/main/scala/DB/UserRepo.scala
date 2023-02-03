package DB

import io.getquill._
import io.getquill.jdbczio.Quill
import zio.{ZIO, ZLayer}

import java.sql.SQLException
import java.time.Instant

object UserRepo {

  case class Users(
      id: Long,
      username: String,
      creationDate: Instant
  )

  case class QueryService(quill: Quill.Postgres[SnakeCase]) {
    import quill._

    def insertUser = quote { (user: Users) =>
      query[Users].insertValue(user).returningGenerated(_.id)
    }
  }
  object QueryService {
    def live: ZLayer[Quill.Postgres[SnakeCase], Nothing, QueryService] =
      ZLayer.fromFunction(QueryService(_))
  }

  case class DataService(queryService: QueryService) {
    import queryService.quill._

    def insertUser(user: Users) = run(
      queryService.insertUser(lift(user))
    )
  }

  object DataService {

    def insertUser(user: Users): ZIO[DataService, SQLException, Long] =
      ZIO.serviceWithZIO[DataService](_.insertUser(user))

    def live: ZLayer[QueryService, Nothing, DataService] =
      ZLayer.fromFunction(DataService(_))
  }

}
