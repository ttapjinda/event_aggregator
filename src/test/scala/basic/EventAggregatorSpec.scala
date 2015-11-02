package com.tayida.eventaggregator
package basic

import org.scalatest._
import spray.testkit.ScalatestRouteTest
import spray.http._
import MediaTypes._
import ContentTypes._
import StatusCodes._
import EventProtocol._

class EventAggregatorSpec extends FreeSpec with Matchers with ScalatestRouteTest with EventAggregatorService {
  def actorRefFactory = system

  // test data
  val eventList = List(
    Event("event0", 1446131568000L), //29 Oct 2015 15:12:48
    Event("event0", 1446131520000L), //29 Oct 2015 15:12:00
    Event("event0", 1446131579000L), //29 Oct 2015 15:12:59
    Event("event0", 1446131579999L), //29 Oct 2015 15:12:59
    Event("event0", 1446131344000L), //29 Oct 2015 15:09:04
    Event("event1", 1446127744000L), //29 Oct 2015 14:09:04
    Event("event1", 1446124144000L), //29 Oct 2015 13:09:04
    Event("event1", 1446124084000L), //29 Oct 2015 13:08:04
    Event("event1", 1446127780000L), //29 Oct 2015 14:09:40
    Event("event1", 1446124084000L) //29 Oct 2015 13:08:04 //duplicated
  )

  "The EventAggregator" - {
    "when there is no event data and calling GET /CountEvent" - {
      "it should return empty json hash" in {
        Get("/CountEvent?EventType=MyEvent2&StartTime=1436131568000&EndTime=1456131568000") ~> eventAggregatorRoute ~> check {
          status === StatusCodes.OK
          mediaType === MediaTypes.`application/json`
          val response = responseAs[Map[String, Int]]
          response.size should equal(0)
        }
      }
    }
    "when calling POST SendEvent" - {
      "it should return Created if that event wasn't already added in the eventList" in {
        for (i <- 0 to 8) {
          Post("/SendEvent", eventList(i)) ~> eventAggregatorRoute ~> check {
            status should equal(StatusCodes.Created)
          }
        }
      }
      "it should return Conflict if that event was already added in the eventList" in {
        Post("/SendEvent", eventList(9)) ~> eventAggregatorRoute ~> check {
          status should equal(StatusCodes.Conflict)
        }
      }
    }
    "when calling GET GetEvent" - {
      "it should return json with the list of event" in {
        Get("/GetEvent") ~> eventAggregatorRoute ~> check {
          status === StatusCodes.OK
          mediaType === MediaTypes.`application/json`
          val response = responseAs[List[Event]]
          response.size should equal(9)
          response(0) === eventList(0)
          response(1) === eventList(1)
          response(2) === eventList(2)
          response(3) === eventList(3)
          response(4) === eventList(4)
          response(5) === eventList(5)
          response(6) === eventList(6)
          response(7) === eventList(7)
          response(8) === eventList(8)
        }
      }
    }
    "when calling GET /CountEvent?EventType=event0&StartTime=1446031568000&EndTime=1446231568000" - {
      "it should return number of event with event0 group by minute" in {
        Get("/CountEvent?EventType=event0&StartTime=1446031568000&EndTime=1446231568000") ~> eventAggregatorRoute ~> check {
          status === StatusCodes.OK
          mediaType === MediaTypes.`application/json`
          val response = responseAs[Map[String, Int]]
          response.size should equal(2)
          response.getOrElse(dateToString(roundDateToMinute(eventList(0).Timestamp)),null) should equal(4)
          response.getOrElse(dateToString(roundDateToMinute(eventList(4).Timestamp)),null) should equal(1)
          response.getOrElse(dateToString(roundDateToMinute(eventList(5).Timestamp)),null) === null
        }
      }
    }
    "when calling GET /CountEvent?EventType=event1&StartTime=1446031568000&EndTime=1446231568000" - {
      "it should return number of event with event1 group by minute" in {
        Get("/CountEvent?EventType=event1&StartTime=1446031568000&EndTime=1446231568000") ~> eventAggregatorRoute ~> check {
          status === StatusCodes.OK
          mediaType === MediaTypes.`application/json`
          val response = responseAs[Map[String, Int]]
          response.size should equal(3)
          response.getOrElse(dateToString(roundDateToMinute(eventList(0).Timestamp)),null) === null
          response.getOrElse(dateToString(roundDateToMinute(eventList(5).Timestamp)),null) should equal(2)
          response.getOrElse(dateToString(roundDateToMinute(eventList(6).Timestamp)),null) should equal(1)
          response.getOrElse(dateToString(roundDateToMinute(eventList(7).Timestamp)),null) should equal(1)
        }
      }
    }
  }
}

