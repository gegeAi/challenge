package com.jliermann.indexer

import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date

import com.jliermann.parser.{Transaction, Transactions}
import com.jliermann.util.GenericFilesOperations
import org.slf4j.{Logger, LoggerFactory}


class TransactionsReader(filesRoot: String) {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  val fileDateFormat = new SimpleDateFormat("yyyyMMdd")
  fileDateFormat.setLenient(false)

  def iterateOnTransactions(date: Date)(lineOperations: Option[Transaction] => Unit): Unit = {

    val filePath = Paths.get(filesRoot, getTransactionsFileName(date))
    GenericFilesOperations.foreachReading(filePath.toFile) {
      line => lineOperations(Transactions.mapLine(line))
    }

  }

  private def getTransactionsFileName(date: Date): String = {
    s"transactions_${fileDateFormat.format(date)}.data"
  }

}

object TransactionsReader {

  def apply(filesRoot: String = "/home/jerome/projects/phenix-challenge/data"): TransactionsReader = {
    new TransactionsReader(filesRoot)
  }

  def apply(maybePath: Option[String]): TransactionsReader = {
    maybePath match {
      case Some(path) => TransactionsReader(path)
      case None => TransactionsReader()
    }
  }
}