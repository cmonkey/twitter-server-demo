package org.github.cmonkey.twitter.server

import java.net.InetSocketAddress

import com.twitter.conversions.time._

import com.twitter.finagle.Service
import com.twitter.finagle.http.{HttpMuxer, Request, Response, Status}
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Future, Time}

object AdvancedServer  extends TwitterServer{

  val what = flag("what", "hello", "String to return")
  val addr = flag("bind", new InetSocketAddress(0), "Bind Arress")
  val durations = flag("alarms", (1.second, 5.second), "2 alarm durations")
  val counter = statsReceiver.counter("requests_counter")

  override def failfastOnFlagsNotParsed: Boolean = true

  val service = new Service[Request, Response] {

    def apply(request: Request) = {
      debug("Received a request at " + Time.now)
      counter.incr()

      val response = Response(request.version, Status.Ok)

      response.contentString= what() + "\n"

      Future.value(response)
    }
  }

  def main(){
    HttpMuxer.addHandler("/echo", service)
    HttpMuxer.addHandler("/echo/", service)
    Await.ready(adminHttpServer)
  }

}
