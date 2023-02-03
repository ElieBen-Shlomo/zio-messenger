package Model

import Model.UserId.UserId

object State {
  type State = Map[UserId, UserSession]
}
