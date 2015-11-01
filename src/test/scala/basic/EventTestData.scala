package com.tayida.eventaggregator
package basic

package com.tayida.eventaggregator.Event
import EventProtocol._

object EventTestData {

  // the test data
  val events = List(
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
}