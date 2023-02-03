package Model

import io.estatico.newtype.macros.newtype
import zio.json.{JsonCodec, JsonDecoder, JsonEncoder}

object UserId {

  @newtype case class UserId(id: Long)

  implicit val InstantJsonCodec: JsonCodec[UserId] = JsonCodec(
    JsonEncoder.long.contramap[UserId](_.id),
    JsonDecoder.long.map(UserId.apply)
  )
}
