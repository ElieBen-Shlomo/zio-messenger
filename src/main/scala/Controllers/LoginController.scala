package Controllers

import DB.{MessageRepo, UserRepo}
import Model.LoginDetails
import Model.UserId.UserId
import Services.UserServiceDB
import zio.http.model.{Cookie, Status}
import zio.http.{Body, Request, Response}
import zio.json._
import zio.{Duration, ZIO}

import scala.language.postfixOps

object LoginController {

  private final val cookieName = "Login Cookie"

  def login(
      req: Request
  ): ZIO[UserRepo.DataService with MessageRepo.DataService, Throwable, Response] = {
    val loginDetails = LoginController.parseLogin(req)

    val insertUserReturningId = for {
      login  <- loginDetails
      userId <- UserServiceDB.createUser(login)
    } yield userId

    def loginResponse(userId: Long) = loginDetails
      .map(_.copy(userId = UserId(userId))) // use id generated from database
      .map(LoginController.makeCookie)
      .map(Response.ok.addCookie(_))
      .catchSome { case _: IllegalArgumentException =>
        ZIO.from(Response(status = Status.BadRequest, body = Body.fromString("Bad Request")))
      }

    for {
      userId   <- insertUserReturningId
      response <- loginResponse(userId)
    } yield response
  }

  private def parseLogin(req: Request): ZIO[Any, Throwable, LoginDetails] =
    for {
      loginEither <- req.body.asString.map(_.fromJson[LoginDetails]).map(stringToThrowable)
      login       <- ZIO.fromEither(loginEither)
    } yield LoginDetails(username = login.username)

  private def makeCookie(loginDetails: LoginDetails): Cookie[Response] =
    Cookie(name = cookieName, content = loginDetails.toJson)
      .withMaxAge(Duration.Infinity)

  private def stringToThrowable[A](either: Either[String, A]): Either[IllegalArgumentException, A] =
    either match {
      case Right(r)  => Right(r)
      case Left(str) => Left(new IllegalArgumentException(str))
    }
}
