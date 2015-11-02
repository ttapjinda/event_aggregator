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

class DateActorSpec extends TestKit(ActorSystem("DateActorSpec"))
    with ImplicitSender
    with FreeSpecLike
    with Matchers
    with BeforeAndAfterAll {

  import DateActor._

  val actorRef = TestActorRef[DateActor]

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
  // implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)

  "DateActor" - {
    "RoundEpochToMinute" - {
      "it should response with DateResult having a correct date" in {
        val result = Await.result(actorRef ? RoundEpochToMinute(1445131568000L), timeout.duration).asInstanceOf[DateResult]
        result.result should equal(new java.util.Date(1445131560000L))
      }
    }
    "DateToString" - {
      "it should response with DateStringResult having value 2015-10-18 01:26" in {
        val result = Await.result(actorRef ? DateToString(new java.util.Date(1445131568000L)), timeout.duration).asInstanceOf[DateStringResult]
        result.result should equal("2015-10-18 01:26")
      }
    }
    "RoundEpochToMinuteToString" - {
      "it should response with DateStringResult having value 2015-10-18 01:26" in {
        val result = Await.result(actorRef ? RoundEpochToMinuteToString(1445131568000L), timeout.duration).asInstanceOf[DateStringResult]
        result.result should equal("2015-10-18 01:26")
      }
    }
  }
}
