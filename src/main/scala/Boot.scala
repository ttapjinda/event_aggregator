package com.tayida.eventaggregator

// import scala.concurrent.duration._ 

import akka.actor.{ Actor, ActorSystem, Props }
import akka.io.IO
import spray.can.Http
import basic._

object Boot extends App {
  implicit val system = ActorSystem("eventaggregator-system")

  /* Use Akka to create our Spray Service */
  val service = system.actorOf(Props[EventAggregatorActor], "eventaggregator-service")

  /* and bind to Akka's I/O interface */
  IO(Http) ! Http.Bind(service, system.settings.config.getString("app.interface"), system.settings.config.getInt("app.port"))

}