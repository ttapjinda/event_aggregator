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

/* Our Server Actor is pretty lightweight; simply mixing in our route trait and logging */
class EventAggregatorActor extends Actor with EventAggregatorService with ActorLogging {
  def actorRefFactory = context
  def receive = runRoute(eventAggregatorRoute)
}

/* Our route directives, the heart of the service.
 * Note you can mix-in dependencies should you so chose */
trait EventAggregatorService extends HttpService {
  import EventProtocol._
  // import WorkerActor._
  var events = Vector[Event]()

  //These implicit values allow us to use futures
  //in this trait.
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)

  val eventAggregatorRoute = {
    path("SendEvent") {
      post {
        // get Event from request's body
        entity(as[Event]) { event =>
          requestContext =>
          // val responder = createResponder(requestContext)
          // add Event to events' list
          addEvent(event) match {
            // Event added
            case true  => {
              requestContext.complete(StatusCodes.Created)
            }
            // Event added fail
            case _  => {
              requestContext.complete(StatusCodes.Conflict)
            }
          }
        }
      }
    } ~
    path("CountEvent") {
      get {
        // Get parameters from URL
        parameters('EventType, 'StartTime.as[Long], 'EndTime.as[Long]) { (eventtype, starttime, endtime) =>
          respondWithMediaType(`application/json`) {
            complete {
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
