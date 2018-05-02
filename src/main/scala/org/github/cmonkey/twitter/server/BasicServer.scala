package org.github.cmonkey.twitter.server

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Future}

object BasicServer extends TwitterServer{

  val service = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {

      val response = Response(request.version,Status.Ok)
      response.contentString = "hello world"
      Future.value(response)
    }
  }

  def main(): Unit ={
    val server = Http.serve(":8888", service)

    onExit(
      server.close()
    )

    Await.ready(server)
  }

}
