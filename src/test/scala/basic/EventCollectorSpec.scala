package com.tayida.eventaggregator
package basic

import org.scalatest.FreeSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Await

class EventCollectorSpec extends TestKit(ActorSystem("EventCollectorSpec"))
    with ImplicitSender
    with FreeSpecLike
    with Matchers
    with BeforeAndAfterAll {

  import EventCollectorActor._

  val actorRef = TestActorRef[EventCollectorActor]

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
  // implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)

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

  "EventCollectorActor" - {
    "AddEvent" - {
      "it should response with EventAdded if event not exists" in {
        for (i <- 0 to 8) {
          val result = Await.result(actorRef ? AddEvent(eventList(i)), timeout.duration)
          result.toString should equal("EventAdded")
        }
      }
      "it should response with EventAlreadyExists if event already exists" in {
        val result = Await.result(actorRef ? AddEvent(eventList(0)), timeout.duration)
        result.toString should equal("EventAlreadyExists")
      }
    }
    "QueryEvent" - {
      "it should response with QueryResult contains a Vector with length 5" in {
        val result = Await.result(actorRef ? QueryEvent("event0", 1445131568000L, 1447131568000L), timeout.duration).asInstanceOf[QueryResult]
        result.result.length should equal(5)
        result.result(0) should equal(eventList(0))
      }
      "it should response with QueryResult contains a Vector with length 1" in {
        val result = Await.result(actorRef ? QueryEvent("event0", 1445131568000L, 1446131520000L), timeout.duration).asInstanceOf[QueryResult]
        result.result.length should equal(1)
        result.result(0) should not equal(eventList(0))
        result.result(0) should equal(eventList(4))
      }
      "it should response with QueryResult contains a Vector with length 0" in {
        val result = Await.result(actorRef ? QueryEvent("event2", 1445131568000L, 1447131568000L), timeout.duration).asInstanceOf[QueryResult]
        result.result.length should equal(0)
      }
    }
  }
}
