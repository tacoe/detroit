package com.beyondtrees.detroit

import net.liftweb.json.JsonAST._


/**
 * Created by IntelliJ IDEA.
 * User: anne
 * Date: 1/28/12
 * Time: 21:59 PM
 * To change this template use File | Settings | File Templates.
 */

case class Message(id:Int,timestamp:Long,data:JValue) {
  def toJSON = JObject(List(
    JField("id",JInt(id)),
    JField("timestamp",JInt(timestamp)),
    JField("data",data)
  ))
}

class Game(val id:String) {
  private val messages = new collection.mutable.Queue[Message]
  def add(data:JValue) = synchronized {
    messages += (Message(
      id = messages.size,
      timestamp = System.currentTimeMillis(),
      data = data
    ))
  }
  def getSince(id:Int) = {
    //copy into local val for blazing performance optimization
    val ms = synchronized {messages}
    ms.dropWhile(_.id < id).toList
  }

}

object Game {
  private val games = new collection.mutable.HashMap[String,Game]

  def getGame(id:String) = synchronized {
    games.get(id).getOrElse(addGame(new Game(id)))
  }
  def addGame(game:Game) = synchronized {
    games += (game.id -> game)
    game
  }
}