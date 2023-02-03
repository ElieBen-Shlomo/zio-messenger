package Services

import DB.UserRepo
import DB.UserRepo.Users
import Model.LoginDetails
import zio.ZIO

import java.sql.SQLException

object UserServiceDB {

  def createUser(loginDetails: LoginDetails): ZIO[UserRepo.DataService, SQLException, Long] = {
    val autoIncrementId = 0L
    val user =
      Users(autoIncrementId, loginDetails.username.username, loginDetails.creationDate)
    UserRepo.DataService.insertUser(user)
  }

}
