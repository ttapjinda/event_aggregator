package com.tayida.eventaggregator
package basic

import scala.concurrent.duration._

import akka.actor.{ Actor, ActorLogging }
import akka.util.Timeout
import spray.http._
import MediaTypes._
import spray.routing._
import spray.http.StatusCodes._
import scala.collection.immutable.ListMap

/* Server Actor */
class EventAggregatorActor extends Actor with EventAggregatorService with ActorLogging {
  def actorRefFactory = context
  def receive = runRoute(eventAggregatorRoute)
}

/* route directives */
trait EventAggregatorService extends HttpService {
  // import json protocal for event class
  import EventProtocol._

  // in-memory data
  // collecting all events received through SendEvent
  var events = Vector[Event]()

  //These implicit values allow us to use futures
  //in this trait.
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)

  val eventAggregatorRoute = {
    // 1. SendEvent endpoint
    path("SendEvent") {
      // a. accept an HTTP POST containing details of an event in JSON
      post {
        // get Event from request's body
        entity(as[Event]) { event =>
          requestContext =>
          // c. This endpoint 'writes' event data to your system if the data is valid and rejects any input which is invalid
          // add Event to events' list
          addEvent(event) match {
            // Event added
            case true  => {
              requestContext.complete(StatusCodes.Created)
            }
            // Event added fail (event already exist in list)
            case _  => {
              requestContext.complete(StatusCodes.Conflict)
            }
          }
        }
      }
    } ~
    // 2. CountEvents endpoint
    path("CountEvent") {
      // a. This endpoint must accept an HTTP GET which has 3 query parameters
      get {
        // Get parameters from URL
        parameters('EventType, 'StartTime.as[Long], 'EndTime.as[Long]) { (eventtype, starttime, endtime) =>
          // b. return a JSON document containing information about all the events with the specified <EventType> between <StartDate> and <EndDate>
          respondWithMediaType(`application/json`) {
            complete {
              // c. group the events by minute and return the count of events in that minute
              // Count Event per minute
              countPerMinute(filterEvent(eventtype, starttime, endtime))
            }
          }
        }
      }
    } ~
    path("GetEvent") {
      get {
        // Get parameters from URL
          respondWithMediaType(`application/json`) {
            complete {
              // Count Event per minute
              events
            }
          }
      }
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

  // Count Event per minute
  private def countPerMinute(input: Vector[Event]): Map[String, Int] = {
    val result = input.groupBy(x => dateToString(roundDateToMinute(x.Timestamp))).mapValues(k => k.length)
    val result2 = ListMap(result.toSeq.sortWith(_._1 < _._1):_*)
    result2
  }

  // Round Date to minute
  def roundDateToMinute(input: Long): java.util.Date = {
    val result = new java.util.Date((java.lang.Math.floor(input/60000)*60000).asInstanceOf[Long])
    result
  }

  // Date to String in order to print out as JSON
  def dateToString(input: java.util.Date): String = {
    var dateformat = new java.text.SimpleDateFormat("YYYY-MM-dd HH:mm");
    dateformat.setTimeZone(java.util.TimeZone.getTimeZone("Etc/UTC"));
    val result = dateformat.format(input)
    result
  }

}
