package com.jliermann.aggregator

import java.util.Date

import com.jliermann.util.DateRange
import org.slf4j.{Logger, LoggerFactory}

class AggregatorEngine(from: Date,
                       to: Date,
                       computeWeeks: Boolean,
                       indexedTransactionsPath: Option[String],
                       pricesPath: Option[String],
                       aggregationSavePath: Option[String]) {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def run(): Unit = {
    val (accurateFrom: Date, accurateTo: Date) = if(computeWeeks) DateRange.adjustToWeeks(from, to) else (from, to)

    val pricesReader = PricesReader(pricesPath)

    val transactionsReader = IndexedTransactionsReader(indexedTransactionsPath)

    DateRange(accurateFrom, accurateTo).iterator().foreach(iterateWithDate(pricesReader, transactionsReader))

    pricesReader.flushMaxProductId()

  }

  def iterateWithDate(pricesReader: PricesReader,
                      indexedTransactionsReader: IndexedTransactionsReader)
                     (date: Date): Unit = {
    indexedTransactionsReader.listStores(date).foreach(
      store => readPrices(date, store, pricesReader, indexedTransactionsReader)
    )
  }

  def readPrices(date: Date,
                 store: String,
                 pricesReader: PricesReader,
                 indexedTransactionsReader: IndexedTransactionsReader): Unit = {
    val aggregator = AggregationWriter(date, store, aggregationSavePath, indexedTransactionsReader)

    pricesReader.iterateOnPrices(date, store)(aggregator.addLineIfExists)
  }

}
