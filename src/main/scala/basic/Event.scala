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

/* Event Class */
// b. The JSON document should have 2 fields
// 		i. EventType - A string which determines the type of event e.g. "PageView"
// 		ii. Timestamp - The time at which the event happened in epoch
case class Event(EventType: String, Timestamp: Long)