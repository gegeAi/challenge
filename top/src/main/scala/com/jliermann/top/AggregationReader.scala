package com.jliermann.top

import java.io.File
import java.nio.file.{Path, Paths}
import java.util.Date

import com.jliermann.aggregator.{AggregationWriter, PricesReader}
import com.jliermann.parser.{QuantitiesWithPrice, QuantityWithPrice}
import com.jliermann.util.GenericFilesOperations
import org.slf4j.{Logger, LoggerFactory}

class AggregationReader(aggregationPath: String) {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def readIndAggForDateRangeAndStores(dates: Iterator[Date], product: Int): QuantityWithPrice = {
    dates
      .map(readIndAggForStores(_, product))
      .foldLeft(QuantityWithPrice(product, "", new Date(), 0, 0))(QuantitiesWithPrice.sum)
  }

  def readIndAggForStores(date: Date, product: Int): QuantityWithPrice = {
    listStores(date)
      .map(readIndexedAggregation(date, _, product))
      .foldLeft(QuantityWithPrice(product, "", date, 0, 0))(QuantitiesWithPrice.sum)
  }

  def readIndAggForDateRange(dates: Iterator[Date], store: String, product: Int): QuantityWithPrice = {
    dates
      .map(readIndexedAggregation(_, store, product))
      .foldLeft(QuantityWithPrice(product, store, new Date(), 0, 0))(QuantitiesWithPrice.sum)
  }

  def readIndexedAggregation(date: Date, store: String, product: Int): QuantityWithPrice = {

    val file: File = AggregationWriter.getSaveFilePath(aggregationPath, date, store, product).toFile

    GenericFilesOperations.reduceReading(file, QuantityWithPrice(product, store, date, 0, 0))(
      (acc, line) => QuantitiesWithPrice.sumIfDefined(acc, QuantitiesWithPrice.mapLine(line)),
      QuantitiesWithPrice.sum
    )
  }


  def listStores(date: Date): Iterator[String] = {
    val dateIndex: File = Paths.get(
      AggregationWriter.aggregationSavePath,
      AggregationWriter.saveDateFormat.format(date)
    ).toFile

    dateIndex.mkdirs()

    dateIndex
      .listFiles
      .map(_.getName).iterator
  }

  def listStores(dates: Iterator[Date]): Iterator[String] = {
    dates
      .map(listStores)
      .reduce (
        (acc: Iterator[String], value: Iterator[String]) => acc ++ value.filterNot(acc.contains)
      )
  }

  def listProducts(): Iterator[Int] = {

    val maxProductPath: Path = Paths.get(PricesReader.maxProductNoticedPath)

    val max = GenericFilesOperations.reduceReading(maxProductPath.toFile, 0)(
      (_: Int, str: String) => str.toInt,
      (acc: Int, _: Int) => acc
    )

    (0 to max).iterator
  }

}

object AggregationReader {

  def apply(path: String = AggregationWriter.aggregationSavePath): AggregationReader = {
    new AggregationReader(path)
  }

  def apply(maybePath: Option[String]): AggregationReader = {
    maybePath match {
      case Some(path) => AggregationReader(path)
      case None => AggregationReader()
    }
  }

}