package com.jliermann.top

import java.io.BufferedWriter
import java.nio.file.{Path, Paths}
import java.text.SimpleDateFormat
import java.util.Date

import com.jliermann.parser.{QuantitiesWithPrice, QuantityWithPrice}
import com.jliermann.util.GenericFilesOperations
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

class TopComputer(savePath: String, global: Boolean) {

  import TopComputer._

  val bufferQty: mutable.ListBuffer[QuantityWithPrice] = mutable.ListBuffer()
  val bufferPrices: mutable.ListBuffer[QuantityWithPrice] = mutable.ListBuffer()

  def addLine(qty: QuantityWithPrice): Unit = {
    val tpsBufferQty = addToBuffer(bufferQty, qty, sortFctQuantity)
    val tpsBufferPrices = addToBuffer(bufferPrices, qty, sortFctPrice)
    bufferQty.clear()
    bufferPrices.clear()
    bufferQty ++= tpsBufferQty
    bufferPrices ++= tpsBufferPrices
  }

  def writeTop(date: Date, week: Boolean, store: Option[String]): Unit = {
    val qtyPath = getTopPath(date, week, store, "ventes")
    val pricesPath = getTopPath(date, week, store, "ca")

    GenericFilesOperations.bufferedWriting(qtyPath.toFile, append = false)(writeList(bufferQty, global))
    GenericFilesOperations.bufferedWriting(pricesPath.toFile, append = false)(writeList(bufferPrices, global))
  }

  def getTopPath(date: Date, week: Boolean, store: Option[String], qtyOrPrice: String): Path = {
    Paths.get(savePath, getTopName(date, week, store, qtyOrPrice))
  }
}

object TopComputer {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  val finalPath = "/tmp/transactions/final"

  val saveDateFormat = new SimpleDateFormat("yyyyMMdd")

  // default args don't work for him ??
  def apply(): TopComputer = {
    new TopComputer(finalPath, global = false)
  }

  def apply(path: String, global: Boolean): TopComputer = {
    new TopComputer(path, global)
  }

  def apply(maybePath: Option[String], global: Boolean = false): TopComputer = {
    maybePath match {
      case Some(path) => TopComputer(path, global)
      case None => TopComputer(finalPath, global)
    }
  }

  def addToBuffer(buffer: mutable.ListBuffer[QuantityWithPrice],
                  aggregation: QuantityWithPrice,
                  sortFct: QuantityWithPrice => Double): mutable.ListBuffer[QuantityWithPrice] = {

    val extendedBuffer = buffer :+ aggregation
    val sortedBuffer = extendedBuffer.sortBy(sortFct)

    if(sortedBuffer.length > 100) sortedBuffer.tail else sortedBuffer

  }

  def sortFctQuantity(quantityWithPrice: QuantityWithPrice): Double = {
    quantityWithPrice.quantity.toDouble
  }

  def sortFctPrice(quantityWithPrice: QuantityWithPrice): Double = {
    quantityWithPrice.totalPrice
  }

  def getTopName(date: Date, week: Boolean, maybeStore: Option[String], qtyOrPrice: String): String = {
    val j7 = if(week) "-J7" else ""
    val storeName = maybeStore match {
      case Some(store) => store
      case None => "GLOBAL"
    }

    s"top_100_${qtyOrPrice}_${storeName}_${saveDateFormat.format(date)}$j7.data"
  }

  def writeList(list: mutable.ListBuffer[QuantityWithPrice], global: Boolean)(writer: BufferedWriter): Unit = {
    list
      .map(qty => QuantitiesWithPrice.mapClass(qty, global))
      .foreach(quantityWithPrice => {
        writer.write(quantityWithPrice)
        writer.newLine()
      })
  }

}
