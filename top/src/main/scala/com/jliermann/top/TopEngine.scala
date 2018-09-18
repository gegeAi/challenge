package com.jliermann.top

import java.util.Date

import com.jliermann.util.DateRange

class TopEngine(from: Date,
                to: Date,
                computeWeeks: Boolean,
                global: Boolean,
                aggregationPath: Option[String],
                savePath: Option[String]) {

  def run(): Unit = {
    val (accurateFrom: Date, accurateTo: Date) = if(computeWeeks) DateRange.adjustToWeeks(from, to) else (from, to)

    DateRange(accurateFrom, accurateTo, computeWeeks).iterator().foreach(switch(computeWeeks, global))
  }


  def switch(weeks: Boolean, global: Boolean)(date: Date): Unit = {
    if(weeks) {
      if(global) {
        computeWeekGlobalTop(date)
      } else {
        computeWeekPerStoreTop(date)
      }
    } else {
      if(global) {
        computeDayGlobalTop(date)
      } else {
        computeDayPerStoreTop(date)
      }
    }
  }

  def computeWeekGlobalTop(monday: Date): Unit = {

    val aggregationReader = AggregationReader(aggregationPath)
    val computer = TopComputer(savePath, global)

    iterateOnProducts(aggregationReader)( product =>
      computer.addLine(aggregationReader.readIndAggForDateRangeAndStores(getWeekIt(monday), product))
    )

    computer.writeTop(monday, week = true, None)

  }

  def computeWeekPerStoreTop(monday: Date): Unit = {

    val aggregationReader = AggregationReader(aggregationPath)
    aggregationReader.listStores(getWeekIt(monday)).foreach { store: String =>

      val computer = TopComputer(savePath, global)

      iterateOnProducts(aggregationReader)(product =>
        computer.addLine(aggregationReader.readIndAggForDateRange(getWeekIt(monday), store, product))
      )
      computer.writeTop(monday, week = true, Some(store))
    }
  }

  def computeDayGlobalTop(day: Date): Unit = {

    val aggregationReader = AggregationReader(aggregationPath)

    val computer = TopComputer(savePath, global)

    iterateOnProducts(aggregationReader)( product =>
      computer.addLine(aggregationReader.readIndAggForStores(day, product))
    )
    computer.writeTop(day, week = false, None)
  }

  def computeDayPerStoreTop(day: Date): Unit = {

    val aggregationReader = AggregationReader(aggregationPath)
    aggregationReader.listStores(day).foreach { store: String =>

      val computer = TopComputer(savePath, global)

      iterateOnProducts(aggregationReader)(product =>
        computer.addLine(aggregationReader.readIndexedAggregation(day, store, product))
      )
      computer.writeTop(day, week = false, Some(store))
    }
  }

  private def getWeekIt(monday: Date): Iterator[Date] = {
    val sunday = new Date(monday.getTime + DateRange.dayIncrement * 6)
    DateRange(monday, sunday).iterator()
  }

  private def iterateOnProducts(aggregationReader: AggregationReader)(fct: Int => Unit): Unit = {
    aggregationReader.listProducts().foreach(fct)
  }

}
