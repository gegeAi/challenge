package com.jliermann.indexer

import java.nio.file.Paths
import java.text.SimpleDateFormat

import com.jliermann.parser.{Transaction, Transactions}
import com.jliermann.util.GenericFilesOperations
import org.scalatest.FlatSpec
import resource._

import scala.io.Source

class IndexedTransactionsWriterSpec extends FlatSpec {

  val dateFormatter = new SimpleDateFormat("yyyyMMdd")

  "getSaveFilePath" should "return an indexed file path given a date, a product and a store of a transaction" in {
    val testTransaction = Transaction(0, dateFormatter.parse("20170514"), "abc", 3, 5)

    assert(IndexedTransactionsWriter("/tmp/transactions-test").getSaveFilePath(testTransaction).toString ==
    "/tmp/transactions-test/20170514/abc/3.data")

    GenericFilesOperations.deleteDirs(Paths.get("/tmp/transactions-test").toFile)

  }

  "addLineIfExists" should "write a transaction into the corresponding file if the transaction is defined" in {
    val testTransaction = Transaction(0, dateFormatter.parse("20170514"), "abc", 3, 5)

    IndexedTransactionsWriter("/tmp/transactions-test").addLineIfExists(Some(testTransaction))

    for {
      reader <- managed(Source.fromFile("/tmp/transactions-test/20170514/abc/3.data"))
    } {
      assert(Transactions.mapLine(reader.getLines().toList.head).get == testTransaction)
    }

    GenericFilesOperations.deleteDirs(Paths.get("/tmp/transactions-test").toFile)
  }

  it should "append lines if many must be written in the same file" in {
    val testTransaction = Transaction(0, dateFormatter.parse("20170514"), "abc", 3, 5)

    IndexedTransactionsWriter("/tmp/transactions-test").addLineIfExists(Some(testTransaction))
    IndexedTransactionsWriter("/tmp/transactions-test").addLineIfExists(Some(testTransaction))

    for {
      reader <- managed(Source.fromFile("/tmp/transactions-test/20170514/abc/3.data"))
    } {
      assert(reader.getLines().length == 2)
    }

    GenericFilesOperations.deleteDirs(Paths.get("/tmp/transactions-test").toFile)
  }

}
