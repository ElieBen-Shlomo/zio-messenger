package main

import Server.WebServer
import Server.WebServer.configLayer
import DB.{MessageRepo, UserRepo}
import io.getquill._
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.DataSource
import zio.http.Server
import zio.{ExitCode, ZIO, ZIOAppArgs, ZIOAppDefault}

object Main extends ZIOAppDefault {

  override def run: ZIO[Environment with ZIOAppArgs, Any, Any] = {

    val program = for {
      processor <- MessageProcessor.Actor.messageProcessor
      _         <- WebServer.startServer(processor)
      _         <- ZIO.never
    } yield ExitCode.success

    program
      .provide(
        UserRepo.DataService.live,
        UserRepo.QueryService.live,
        MessageRepo.DataService.live,
        MessageRepo.QueryService.live,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
        DataSource.fromPrefix("ctx"),
        configLayer,
        Server.live
      )
  }
}
