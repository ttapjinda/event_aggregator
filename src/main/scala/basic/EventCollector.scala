package com.tayida.eventaggregator
package basic

import akka.actor.{ Actor, ActorLogging }

/* EventCollectorActor Actor
    working as the database, collecting all events */
object EventCollectorActor {
  case class AddEvent(event: Event)
  case class QueryEvent(EventType: String, StartTime: Long, EndTime: Long)
  case class QueryResult(result: Vector[Event])
}

class EventCollectorActor extends Actor with ActorLogging {
  import EventCollectorActor._
  import EventProtocol._

  var events = Vector[Event]()

  def receive = {
    case AddEvent(event) => {
      log.info(s"Add ${event}")
      addEvent(event) match {
        case true  => {
          sender ! EventAdded
        }
        case _  => {
          sender ! EventAlreadyExists
        }
      }
    }
    case QueryEvent(eventtype, starttime, endtime) => {
      val result = filterEvent(eventtype, starttime, endtime)
      log.info(s"QueryEvent ${result}")
      sender ! QueryResult(result)
    }
  }
  // add Event to events' list if it does not exist
  private def addEvent(event: Event): Boolean = {
    val doesNotExist = !events.exists(_ == event)
    if (doesNotExist) events = events :+ event
    doesNotExist
  }

  // filter Event with the same EventType and having Timestamp between StartTime and EndTime
  private def filterEvent(EventType: String, StartTime: Long, EndTime: Long): Vector[Event] = {
    val filteredEvents = events.filter(x => (x.EventType == EventType && x.Timestamp > StartTime && x.Timestamp < EndTime))
    filteredEvents
  }
}