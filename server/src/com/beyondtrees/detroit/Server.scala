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
import net.liftweb.json.JsonParser.parse

class DetroitHandler extends DefaultHandler {
  override def handle(target:String,
                      baseRequest: org.eclipse.jetty.server.Request,
                      request: javax.servlet.http.HttpServletRequest,
                      response: javax.servlet.http.HttpServletResponse) {
    //extract parameters
    val game = Game.getGame(target)
    val since = Integer.parseInt(Option(request.getParameter("since")).getOrElse("0"))
    Option(request.getParameter("j")) match {
      case Some(js) if (js != "") =>
        println("Game "+game.id+" request SINCE="+since+" and MESSAGE="+js)
        game.add(parse(js))
      case _ =>
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
    println("Welcome to Detroit. The one and only parameter is the server port number, but if you leave that out we'll pick something suitable for you")
    val port = Integer.parseInt(args.headOption.getOrElse("8080"))

    val s = new Server(port)
    println("aaaaaand we have a server, waiting patiently for you on port "+port)

    s.setHandler(new DetroitHandler)


    s.start()

    println("press ENTER to stop")
    readLine()
    s.stop
    println("Server stopped")
  }
}