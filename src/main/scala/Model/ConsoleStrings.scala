package Model

import Model.Username.Username

object ConsoleStrings {
  def waiting(username: Username) = s"Welcome ${username.username} - waiting to be connected with a pair"
  def disconnected(pairName: Username) =
    s"Chat session has disconnected - no longer chatting with ${pairName.username}"
  def connected(pairName: Username) = s"Connected - You are now chatting with ${pairName.username}"
  def noPairYet = "Not currently matched with anyone - message not sent"
}
