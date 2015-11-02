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

class AggregatorActorSpec extends TestKit(ActorSystem("AggregatorActorSpec"))
    with ImplicitSender
    with FreeSpecLike
    with Matchers
    with BeforeAndAfterAll {

  import AggregatorActor._

  val actorRef = TestActorRef[AggregatorActor]

  val events = Vector(
    Event("event0", 1446131568000L), //29 Oct 2015 15:12:48
    Event("event0", 1446131520000L), //29 Oct 2015 15:12:00
    Event("event0", 1446131579000L), //29 Oct 2015 15:12:59
    Event("event0", 1446131579999L), //29 Oct 2015 15:12:59
    Event("event0", 1446131344000L), //29 Oct 2015 15:09:04
    Event("event0", 1446127744000L), //29 Oct 2015 14:09:04
    Event("event0", 1446124144000L), //29 Oct 2015 13:09:04
    Event("event0", 1446124084000L), //29 Oct 2015 13:08:04
    Event("event0", 1446127780000L) //29 Oct 2015 14:09:40
  )

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  implicit val timeout = Timeout(5 seconds)

  "AggregatorActor" - {
    "AggregateEvent" - {
      "it should response with AggregateResult having a correct Map[date, count]" in {
        val result = Await.result(actorRef ? AggregateEvent(events), timeout.duration).asInstanceOf[AggregateResult]
        result.result.getOrElse("2015-10-29 15:12",null) should equal(4)
        result.result.getOrElse("2015-10-29 14:09",null) should equal(2)
        result.result.getOrElse("2015-10-29 13:09",null) should equal(1)
        result.result.getOrElse("2015-10-29 13:08",null) should equal(1)
        result.result.getOrElse("2015-10-29 16:12",null) === null
      }
    }
  }
}
