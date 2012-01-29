package com.beyondtrees.detroit

/**
 * Created by IntelliJ IDEA.
 * User: anne
 * Date: 1/28/12
 * Time: 21:13 PM
 * To change this template use File | Settings | File Templates.
 */

import org.eclipse.jetty.server._
import handler.DefaultHandler
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

class DetroitHandler extends DefaultHandler {
  override def handle(target:String,
                      baseRequest: org.eclipse.jetty.server.Request,
                      request: javax.servlet.http.HttpServletRequest,
                      response: javax.servlet.http.HttpServletResponse) {
    //extract parameters
    val game = Game.getGame(target)
    val since = Integer.parseInt(Option(request.getParameter("since")).getOrElse("0"))
    val jsonstring = Option(request.getParameter("j"))
    if (jsonstring.isDefined) {
      println("Game "+game.id+" request SINCE="+since+" and MESSAGE="+jsonstring)
      //parse into live json object
      val input = net.liftweb.json.JsonParser.parse(jsonstring.get)
      game.add(input)
    } else {
      println("Game "+game.id+" request SINCE="+since)
    }

    val output = JObject(List(
      JField("jetzt",JInt(System.currentTimeMillis())),
      JField("game",JString(game.id)),
      JField("since",JInt(since)),
      JField("messages",JArray(game.getSince(since).map(_.toJSON)))
    ))

    response.setStatus(200)
    response.setHeader("Access-Control-Allow-Origin","*")
    response.setContentType("application/json")
    response.getWriter.write(pretty(render(output)))
    response.flushBuffer
  }
}

object Server {
  def main(args:Array[String]) {
    println("hi there")

    val s = new Server(8080)
    println("aaaaaand we have a server")

    s.setHandler(new DetroitHandler)


    s.start()

    println("press ENTER to stop")
    readLine()
    s.stop
    println("Server stopped")
  }
}