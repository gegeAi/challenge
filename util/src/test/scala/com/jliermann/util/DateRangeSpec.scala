package com.jliermann.util

import java.text.SimpleDateFormat
import java.util.Date

import org.scalatest.FlatSpec

class DateRangeSpec extends FlatSpec {

  val parser = new SimpleDateFormat("yyyyMMdd")

  "iterator" should "return a value per day with begin and end date included" in {

    val range = DateRange(parser.parse("20170514"), parser.parse("20170517"))
    val expectedValues = List(
      parser.parse("20170514"),
      parser.parse("20170515"),
      parser.parse("20170516"),
      parser.parse("20170517")
    )
    assert(range.iterator().toList == expectedValues)

  }

  it should "return an iterator of one date if begin and end happen on same day" in {
    val range = DateRange(parser.parse("20170514"), parser.parse("20170514"))
    assert(range.iterator().toList.head == parser.parse("20170514"))
  }

  "adjustToWeeks" should "return longer range beginning on a monday, ending on a sunday" in {

    val beginDate = parser.parse("20180912") // Wed. 12 Sep. 2018
    val endDate = parser.parse("20180921") // Thu. 21 Sep. 2018

    val suggestedBeginDate = parser.parse("20180910") // Mon. 10 Sep. 2018
    val suggestedEndDate = parser.parse("20180923") // Sun. 23 Sep. 2018

    val result: (Date, Date) = DateRange.adjustToWeeks(beginDate, endDate)

    assert((suggestedBeginDate, suggestedEndDate) == result)


  }

}
