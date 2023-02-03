package Model

import io.estatico.newtype.macros.newtype
import zio.json.{JsonCodec, JsonDecoder, JsonEncoder}

object Username {

  @newtype case class Username(username: String)

  implicit val InstantJsonCodec: JsonCodec[Username] = JsonCodec(
    JsonEncoder.string.contramap[Username](_.username),
    JsonDecoder.string.map(Username.apply)
  )
}
