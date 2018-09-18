package com.jliermann.util

import java.util.{Calendar, Date}

class DateRange private (startDate: Date, endDate: Date, weeks: Boolean) extends Iterable[Date] {

  import DateRange._

  def iterator(): Iterator[Date] = {
    stream().iterator
  }

  private def stream(): Stream[Date] = {
    val increment = if(weeks) dayIncrement * 7 else dayIncrement

    Stream.iterate(startDate)(date => new Date(date.getTime + increment))
        .takeWhile(date => {
          val calIt = Calendar.getInstance()
          val calEnd = Calendar.getInstance()
          calIt.setTime(date)
          calEnd.setTime(endDate)
          calIt.get(Calendar.YEAR) <= calEnd.get(Calendar.YEAR) &&
          calIt.get(Calendar.DAY_OF_YEAR) <= calEnd.get(Calendar.DAY_OF_YEAR)
        })
  }

}

object DateRange {

  val dayIncrement: Int = 1000 * 60 * 60 * 24

  def apply(startDate: Date, endDate: Date, weeks: Boolean = false): DateRange = {
    if(startDate.after(endDate)) {
      new DateRange(endDate, startDate, weeks)
    } else {
      new DateRange(startDate, endDate, weeks)
    }

  }

  def adjustToWeeks(from: Date, to: Date): (Date, Date) = {
    val calFrom = Calendar.getInstance()
    val calTo = Calendar.getInstance()

    calFrom.setTime(from)
    calTo.setTime(to)

    calFrom.set(Calendar.DAY_OF_WEEK, calFrom.getFirstDayOfWeek)
    calTo.set(Calendar.DAY_OF_WEEK, calFrom.getFirstDayOfWeek+6)

    (calFrom.getTime, calTo.getTime)
  }
}

case class IncompleteDateRange()
  extends Exception("Impossible to continue : date range is not completely defined; " +
    "please use --from / --to or --date to configure date range")