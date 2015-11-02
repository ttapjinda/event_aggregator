package com.tayida.eventaggregator
package basic

import akka.actor.{ Actor, ActorLogging }

/* DateActor Actor
    handle all date related function */
object DateActor {
  case class DateToString(date: java.util.Date)
  case class RoundEpochToMinute(epoch: Long)
  case class RoundEpochToMinuteToString(epoch: Long)
  case class DateResult(result: java.util.Date)
  case class DateStringResult(result: String)
}

class DateActor extends Actor with ActorLogging {
  import DateActor._

  def receive = {
    case DateToString(date) => {
      log.info(s"DateToString ${date}")
      val result = dateToString(date)
      sender ! DateStringResult(result)
    }
    case RoundEpochToMinute(epoch) => {
      log.info(s"RoundEpochToMinute ${epoch}")
      val result = roundEpochToMinute(epoch)
      sender ! DateResult(result)
    }
    case RoundEpochToMinuteToString(epoch) => {
      log.info(s"RoundEpochToMinuteToString ${epoch}")
      val result = dateToString(roundEpochToMinute(epoch))
      sender ! DateStringResult(result)
    }
  }
  // Round Epoch to minute
  private def roundEpochToMinute(input: Long): java.util.Date = {
    val result = new java.util.Date((java.lang.Math.floor(input/60000)*60000).asInstanceOf[Long])
    result
  }
  // Date to String in order to print out as JSON
  private def dateToString(input: java.util.Date): String = {
    var dateformat = new java.text.SimpleDateFormat("YYYY-MM-dd HH:mm");
    dateformat.setTimeZone(java.util.TimeZone.getTimeZone("Etc/UTC"));
    val result = dateformat.format(input)
    result
  }
}