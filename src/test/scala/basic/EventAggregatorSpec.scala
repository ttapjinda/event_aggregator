package com.tayida.eventaggregator
package basic

import org.scalatest.Matchers
import org.scalatest.FreeSpec

import spray.testkit.ScalatestRouteTest
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.http._

import akka.actor.Props
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.duration._

class EventAggregatorSpec extends FreeSpec with Matchers with ScalatestRouteTest with EventAggregatorService {
  def actorRefFactory = system

  import EventProtocol._

  // for CountEvent Testing
  import DateActor._
  val dateActor = actorRefFactory.actorOf(Props[DateActor], "dateActor")

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
    Event("event1", 1446127780000L) //29 Oct 2015 14:09:40
  )

  "The EventAggregatorSpec" - {
    "1. SendEvent endpoint" - {
      "a. This endpoint must accept an HTTP POST containing details of an event in JSON." - {
        "it should response with Created if HTTP POST containing details of an event in JSON"  in {
          for (i <- 0 to 8) {
            Post("/SendEvent", eventList(i)) ~> eventAggregatorRoute ~> check {
              status should equal(StatusCodes.Created)
            }
          }
        }
      }
      "b. The JSON document should have 2 fields, EventType and Timestamp" - {
        "it should response with Created if The JSON document have field and all fields are correct" in {
          Post("/SendEvent", """{ "EventType": "event2", "Timestamp": 1446124084000 }""".parseJson.asJsObject) ~> sealRoute(eventAggregatorRoute) ~> check {
            status should equal(StatusCodes.Created)
          }
        }
        "it should response with BadRequest if The JSON document doesn't match EventType and Timestamp" in {
          Post("/SendEvent", """{ "Eventtype": "event3", "Timestamp": 1446124084000, "status": "open" }""".parseJson.asJsObject) ~> sealRoute(eventAggregatorRoute) ~> check {
            status should equal(StatusCodes.BadRequest)
          }
        }
        "it should response with BadRequest if The JSON document have less than 2 fields" in {
          Post("/SendEvent", """{ "EventType": "event2"}""".parseJson.asJsObject) ~> sealRoute(eventAggregatorRoute) ~> check {
            status should equal(StatusCodes.BadRequest)
          }
        }
      }
      "c. This endpoint 'writes' event data to your system if the data is valid and rejects any input which is invalid" in {
        Post("/SendEvent", """{ "Eventtype": "event1", "Timestamp": 1446124084000 }""".parseJson.asJsObject) ~> sealRoute(eventAggregatorRoute) ~> check {
          status should equal(StatusCodes.BadRequest)
          responseAs[String] should startWith("The request content was malformed:")
        }
      }
    }
    "2. CountEvents endpoint" - {
      "a. This endpoint must accept an HTTP GET which has 3 query parameters" - {
        "it should response with OK Status if the request has 3 query parameters" in {
          Get("/CountEvent?EventType=event0&StartTime=1446031568000&EndTime=1446231568000") ~> eventAggregatorRoute ~> check {
            status should equal(StatusCodes.OK)
          }
        }
        "it should response with NotFound if there is any missing query parameter" in {
          Get("/CountEvent?EventType=event1&StartTime=1446031568000") ~> sealRoute(eventAggregatorRoute) ~> check {
            status should equal(StatusCodes.NotFound)
            responseAs[String] should equal("Request is missing required query parameter 'EndTime'")
          }
        }
      }
      "b. If the GET request is valid then it will return a JSON document containing information about all the events with the specified <EventType> between <StartDate> and <EndDate>" - {
        "it should return JSON document" in {
          Get("/CountEvent?EventType=event0&StartTime=1446031568000&EndTime=1446231568000") ~> eventAggregatorRoute ~> check {
            status should equal(StatusCodes.OK)
            mediaType === MediaTypes.`application/json`
          }
        }
      }
      "c. This JSON response will group the events by minute and return the count of events in that minute" - {
        "it should return the correct number of event with event0 type between StartTime and EndTime group by minute" in {
          Get("/CountEvent?EventType=event0&StartTime=1446031568000&EndTime=1446231568000") ~> eventAggregatorRoute ~> check {
            status should equal(StatusCodes.OK)
            mediaType === MediaTypes.`application/json`
            val response = responseAs[Map[String, Int]]
            response.size should equal(2)
            response.getOrElse(roundEpochToMinuteToString(eventList(0).Timestamp),null) should equal(4)
            response.getOrElse(roundEpochToMinuteToString(eventList(4).Timestamp),null) should equal(1)
            response.getOrElse(roundEpochToMinuteToString(eventList(5).Timestamp),null) === null
          }
        }
        "it should return the correct number of event with event1 type between StartTime and EndTime group by minute" in {
          Get("/CountEvent?EventType=event1&StartTime=1446031568000&EndTime=1446231568000") ~> eventAggregatorRoute ~> check {
            status should equal(StatusCodes.OK)
            mediaType === MediaTypes.`application/json`
            val response = responseAs[Map[String, Int]]
            response.size should equal(3)
            response.getOrElse(roundEpochToMinuteToString(eventList(0).Timestamp),null) === null
            response.getOrElse(roundEpochToMinuteToString(eventList(5).Timestamp),null) should equal(2)
            response.getOrElse(roundEpochToMinuteToString(eventList(6).Timestamp),null) should equal(1)
            response.getOrElse(roundEpochToMinuteToString(eventList(7).Timestamp),null) should equal(1)
          }
        }
        "it should return empty json if there is no event with that EventType between StartTime and EndTime" in {
          Get("/CountEvent?EventType=event3&StartTime=1436131568000&EndTime=1456131568000") ~> eventAggregatorRoute ~> check {
            status === StatusCodes.OK
            mediaType === MediaTypes.`application/json`
            val response = responseAs[Map[String, Int]]
            response.size should equal(0)
          }
        }
      }
    }
  }
  // function for CountEvent Testing
  // Round Epoch to minute to String
  private def roundEpochToMinuteToString(input: Long): String = {
    val result = Await.result(dateActor ? RoundEpochToMinuteToString(input), timeout.duration).asInstanceOf[DateStringResult]
    result.result
  }
}

