package com.jliermann.indexer

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Calendar

import com.jliermann.parser.Transaction
import com.jliermann.util.GenericFilesOperations
import org.scalatest.{FlatSpec, PrivateMethodTester}
import resource.managed

class TransactionsReaderSpec extends FlatSpec with PrivateMethodTester {

  val dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmssz")

  "getTransactionsFileName" should "return a correctly parsed filename given a date" in {

    val cal = Calendar.getInstance()
    cal.set(2017, 4, 14)

    val getTransactionsFileName = PrivateMethod[String]('getTransactionsFileName)

    val filename = TransactionsReader() invokePrivate getTransactionsFileName(cal.getTime)

    assert(filename == "transactions_20170514.data")

  }

  "iterateOnTransactions" should "try to parse each line of a file and do a specific operation on them" in {
    val testLines = List(
      "2900|20170514T175316CEST|abc|1|2",
      "3128|20170514T030001CEST|def|1|5")

    val comparison = List(
      Transaction(2900, dateFormatter.parse("20170514T175316CEST"), "abc", 1, 2),
      Transaction(3128, dateFormatter.parse("20170514T030001CEST"), "def", 1, 5)
    )

    val testFile: File = Paths.get("/tmp/transactions-test/transactions_20170514.data").toFile
    testFile.getParentFile.mkdirs
    testFile.createNewFile

    for {
      fileWriter <- managed(new FileWriter(testFile))
      bw <- managed(new BufferedWriter(fileWriter))
    } {
      testLines.foreach(line => {
        bw.write(line)
        bw.newLine()
      })
    }

    TransactionsReader("/tmp/transactions-test")
      .iterateOnTransactions(dateFormatter.parse("20170514T030001CEST")) { maybeTransaction =>
        maybeTransaction.foreach(transaction => assert(comparison.contains(transaction)))
      }

    GenericFilesOperations.deleteDirs(Paths.get("/tmp/transactions-test").toFile)
  }

}
