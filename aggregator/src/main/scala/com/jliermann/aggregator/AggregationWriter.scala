package com.jliermann.aggregator
import java.nio.file.{Path, Paths}
import java.text.SimpleDateFormat
import java.util.Date

import com.jliermann.parser._
import com.jliermann.util.GenericFilesOperations.bufferedWriting
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Try}

class AggregationWriter(date: Date,
                        store: String,
                        savePath: String,
                        indexedTransactionsReader: IndexedTransactionsReader) {

  import AggregationWriter._

  def addLineIfExists(maybePrice: Option[Price]): Unit = {
    maybePrice.foreach { price =>

      val associatedTransaction =
        indexedTransactionsReader.readIndexedTransaction(date, store, price.product)

      val joined: Option[QuantityWithPrice] =
        QuantitiesWithPrice.fromQuantityAndPrice(associatedTransaction, price)

      joined.foreach(writeLine)
    }
  }

  def getSaveFilePath(qty: QuantityWithPrice): Path = {
    AggregationWriter.getSaveFilePath(savePath, qty.reducedTimeStamp, qty.store, qty.product)
  }

  private def writeLine(qty: QuantityWithPrice): Boolean = {

    Try(
      bufferedWriting(getSaveFilePath(qty).toFile) { br =>
        br.write(QuantitiesWithPrice.mapClass(qty))
        br.newLine()
      }
    ) match {
      case Failure(e) =>
        logger.error(s"Can't write line <\n$qty\n>. Cause : <${e.getMessage}>")
        false
      case _ => true
    }
  }


}

object AggregationWriter {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  val aggregationSavePath = "/tmp/transactions/aggregated-transactions"

  val saveDateFormat = new SimpleDateFormat("yyyyMMdd")

  def apply(date: Date,
            store: String,
            savePath: String,
            indexedTransactionsReader: IndexedTransactionsReader): AggregationWriter = {
    new AggregationWriter(date, store, savePath, indexedTransactionsReader)
  }

  def apply(date: Date,
            store: String,
            indexedTransactionsReader: IndexedTransactionsReader): AggregationWriter = {
    AggregationWriter(date, store, aggregationSavePath, indexedTransactionsReader)
  }

  def apply(date: Date,
            store: String,
            savePath: Option[String],
            indexedTransactionsReader: IndexedTransactionsReader): AggregationWriter = {
    savePath match {
      case Some(path) => AggregationWriter(date, store, path, indexedTransactionsReader)
      case None => AggregationWriter(date, store, indexedTransactionsReader)
    }
  }

  def getSaveFilePath(path: String, date: Date, store: String, product: Int): Path = {
    Paths.get(path,
      saveDateFormat.format(date),
      store,
      product.toString + ".data")
  }

}
