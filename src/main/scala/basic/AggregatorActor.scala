package com.tayida.eventaggregator
package basic

import akka.actor.{ Actor, Props, ActorLogging }
import akka.util.Timeout
import akka.pattern.ask
import scala.collection.immutable.ListMap
import scala.concurrent.Await
import scala.concurrent.duration._

/* AggregatorActor Actor
    count events per minute */
object AggregatorActor {
  case class AggregateEvent(events: Vector[Event])
  case class AggregateResult(result: Map[String, Int])
}

class AggregatorActor extends Actor with ActorLogging {
  def actorRefFactory = context

  import AggregatorActor._
  import DateActor._
  import EventProtocol._

  val dateActor = actorRefFactory.actorOf(Props[DateActor], "dateActor")

  //These implicit values allow us to use futures
  // implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)

  def receive = {
    case AggregateEvent(events) => {
      log.info(s"AggregateEvent ${events}")
      val result = countPerMinute(events)
      sender ! AggregateResult(result)
    }
  }
  // Count Event per minute
  private def countPerMinute(input: Vector[Event]): Map[String, Int] = {
    val result = input.groupBy(x =>roundEpochToMinuteToString(x.Timestamp))
                      .mapValues(k => k.length)
    val result2 = ListMap(result.toSeq.sortWith(_._1 < _._1):_*)
    result2
  }

  // Round Epoch to minute to String
  private def roundEpochToMinuteToString(input: Long): String = {
    val result = Await.result(dateActor ? RoundEpochToMinuteToString(input), timeout.duration).asInstanceOf[DateStringResult]
    result.result
  }

  // Round Epoch to minute
  private def roundDateToMinute(input: Long): java.util.Date = {
    val result = Await.result(dateActor ? RoundEpochToMinute(input), timeout.duration).asInstanceOf[DateResult]
    result.result
  }

  // Date to String in order to print out as JSON
  private def dateToString(input: java.util.Date): String = {
    val result = Await.result(dateActor ? DateToString(input), timeout.duration).asInstanceOf[DateStringResult]
    result.result
  }
}