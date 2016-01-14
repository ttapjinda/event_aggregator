package com.tayida.eventaggregator
package basic

import akka.actor.{ Actor, ActorLogging, Props }
import akka.util.Timeout
import akka.pattern.ask
import spray.http._
import spray.routing._
import spray.httpx.SprayJsonSupport._
import scala.concurrent.duration._
import scala.concurrent.Await

/* Server Actor */
class EventAggregatorActor extends Actor with ActorLogging with EventAggregatorService {
  def actorRefFactory = context
  def receive = runRoute(eventAggregatorRoute)
}

/* route directives */
trait EventAggregatorService extends HttpService {
  // import json protocal for event class
  import EventProtocol._
  // import EventCollector used as a data collection
  import EventCollectorActor._
  // import Aggregator used to aggregate the event data
  import AggregatorActor._

  // // in-memory data
  // // collecting all events received through SendEvent
  // var events = Vector[Event]()

  // init used actors
  val eventCollector = actorRefFactory.actorOf(Props[EventCollectorActor], "eventCollector")
  val aggregatorActor = actorRefFactory.actorOf(Props[AggregatorActor], "aggregatorActor")

  //These implicit values allow us to use futures
  //in this trait.
  // implicit def executionContext = actorRefFactory.dispatcher
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
          // send event to eventCollector
          Await.result(eventCollector ? AddEvent(event), timeout.duration) match {
            case EventAdded  => {
              requestContext.complete(StatusCodes.Created)
            }
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
          respondWithMediaType(MediaTypes.`application/json`) {
            complete {
              countPerMinute(doQueryEvent(eventtype, starttime, endtime))
            }
          }
        }
      }
    }
  }

  // Count Event per minute
  private def countPerMinute(input: Vector[Event]): Map[String, Int] = {
    val result = Await.result(aggregatorActor ? AggregateEvent(input), timeout.duration).asInstanceOf[AggregateResult]
    result.result
  }

  // query events from eventCollector
  def doQueryEvent(EventType: String, StartTime: Long, EndTime: Long) = {
    val result = Await.result(eventCollector ? QueryEvent(EventType, StartTime, EndTime), timeout.duration).asInstanceOf[QueryResult]
    result.result
  }

}
