package com.tayida.eventaggregator
package basic

import spray.json.{ JsonFormat, DefaultJsonProtocol }

/* Event Class */
// b. The JSON document should have 2 fields
// 		i. EventType - A string which determines the type of event e.g. "PageView"
// 		ii. Timestamp - The time at which the event happened in epoch
case class Event(EventType: String, Timestamp: Long)

case object EventAdded

case object EventAlreadyExists

object EventProtocol extends DefaultJsonProtocol {
  implicit val eventFormat = jsonFormat2(Event.apply)
}