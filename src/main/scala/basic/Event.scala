package com.tayida.eventaggregator
package basic

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{ read, write }
import spray.httpx.Json4sSupport
/* Used to mix in Spray's Marshalling Support with json4s */
object EventProtocol extends Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats
}

/* Our case class, used for request and responses */
case class Event(EventType: String, Timestamp: Long)