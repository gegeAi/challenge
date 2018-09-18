package com.jliermann.aggregator

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Paths
import java.text.SimpleDateFormat

import com.jliermann.parser.Price
import com.jliermann.util.GenericFilesOperations
import org.scalatest.{FlatSpec, PrivateMethodTester}
import resource._

class PricesReaderSpec extends FlatSpec with PrivateMethodTester {

  val dateFormatter = new SimpleDateFormat("yyyyMMdd")

  "mapLineAndNoticeId" should "convert a string line to some Price and keep the product id if it is higher" in {

    val mapLineAndNoticeId = PrivateMethod[Option[Price]]('mapLineAndNoticeId)

    val comparison = Price(359, 2)
    val ref = "359|2"

    val priceReader = PricesReader("tmp/transactions-test")

    val result: Option[Price] = priceReader invokePrivate mapLineAndNoticeId(ref)

    assert(result.get == comparison)
    assert(priceReader.maxProductId.intValue == 359)

    GenericFilesOperations.deleteDirs(Paths.get("/tmp/transactions-test").toFile)

  }

}
