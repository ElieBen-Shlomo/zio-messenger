package Model

import Model.UserId.UserId
import Model.Username.Username
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.time.Instant

case class LoginDetails(
    username: Username,
    userId: UserId = UserId(0),
    creationDate: Instant = Instant.now
)

object LoginDetails {
  implicit val loginDetailsEncoder: JsonEncoder[LoginDetails] = DeriveJsonEncoder.gen[LoginDetails]
  implicit val loginDetailsDecoder: JsonDecoder[LoginDetails] = DeriveJsonDecoder.gen[LoginDetails]
}
