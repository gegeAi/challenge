package com.jliermann.aggregator

import java.nio.file.{Path, Paths}
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import com.jliermann.parser.{Price, Prices}
import com.jliermann.util.GenericFilesOperations
import org.slf4j.{Logger, LoggerFactory}

class PricesReader(dataPath: String) {

  import PricesReader._

  var maxProductId: AtomicInteger = new AtomicInteger(0)

  def iterateOnPrices(date: Date, store: String)(lineOperations: Option[Price] => Unit): Unit = {

    val filePath: Path = Paths.get(dataPath, getPricesFileName(date, store))
    GenericFilesOperations.foreachReading(filePath.toFile) {
      line => lineOperations(mapLineAndNoticeId(line))
    }

  }

  def flushMaxProductId(): Unit = {

    val maxProductPath: Path = Paths.get(maxProductNoticedPath)

    val otherMax = GenericFilesOperations.reduceReading(maxProductPath.toFile, 0)(
      (_: Int, str: String) => str.toInt,
      (acc: Int, _: Int) => acc
    )

    val effectiveMax = if(maxProductId.intValue > otherMax) maxProductId else otherMax

    GenericFilesOperations.bufferedWriting(maxProductPath.toFile, append = false) { br =>
      br.write(effectiveMax.toString)
      br.newLine()
    }
  }

  private def mapLineAndNoticeId(line: String): Option[Price] = {
    val maybePrice = Prices.mapLine(line)
    maybePrice.map(accumulate)
  }

  private def getPricesFileName(date: Date, store: String): String = {
    s"reference_prod-${store}_${fileDateFormat.format(date)}.data"
  }

  private def accumulate(price: Price): Price = {
    val currentValue = maxProductId.intValue
    val applicableNewValue = if(currentValue > price.product) currentValue else price.product
    maxProductId.set(applicableNewValue)
    price
  }

}

object PricesReader {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  val defaultPath: String = "/home/jerome/projects/phenix-challenge/data"

  val fileDateFormat = new SimpleDateFormat("yyyyMMdd")
  fileDateFormat.setLenient(false)

  val maxProductNoticedPath = "/tmp/transactions/maxProductId"

  def apply(path: String = defaultPath): PricesReader = {
    new PricesReader(path)
  }

  def apply(maybePath: Option[String]): PricesReader = {
    maybePath match {
      case Some(path) => PricesReader(path)
      case None => PricesReader()
    }
  }

}
