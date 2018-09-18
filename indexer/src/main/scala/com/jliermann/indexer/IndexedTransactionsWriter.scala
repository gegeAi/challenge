package com.jliermann.indexer

import java.io._
import java.nio.file.{Path, Paths}
import java.text.SimpleDateFormat
import java.util.Date

import com.jliermann.parser.{Transaction, Transactions}
import com.jliermann.util.GenericFilesOperations
import com.jliermann.util.GenericFilesOperations.bufferedWriting
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Try}

class IndexedTransactionsWriter(savePath: String) {

  import IndexedTransactionsWriter._


  def addLineIfExists(transaction: Option[Transaction]): Unit = {
    transaction.foreach(writeLine)
  }

  private def writeLine(transaction: Transaction): Boolean = {

    Try(
      bufferedWriting(getSaveFilePath(transaction).toFile) {
        writeTransaction(transaction)
      }
    ) match {
      case Failure(e) =>
        logger.error(s"Can't write line <\n$transaction\n>. Cause : <${e.getMessage}>")
        false
      case _ => true
    }
  }

  def getSaveFilePath(transaction: Transaction): Path = {
    getSaveFilePath(transaction.timeStamp, transaction.store, transaction.product)
  }

  def getSaveFilePath(date: Date, store: String, product: Int): Path = {
    Paths.get(savePath,
      saveDateFormat.format(date),
      store,
      product.toString + ".data")
  }

  def writeTransaction(transaction: Transaction)(br: BufferedWriter): Unit = {
    br.write(Transactions.mapClass(transaction))
    br.newLine()
  }

}

object IndexedTransactionsWriter {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  val temporaryFilesPath = "/tmp/transactions/indexed-transactions"

  val saveDateFormat = new SimpleDateFormat("yyyyMMdd")
  saveDateFormat.setLenient(false)

  def apply(savePath: String = temporaryFilesPath): IndexedTransactionsWriter = {

    GenericFilesOperations.deleteDirs(Paths.get(savePath).toFile)
    new IndexedTransactionsWriter(savePath)
  }

  def apply(maybePath: Option[String]): IndexedTransactionsWriter = {
    maybePath match {
      case Some(path) => IndexedTransactionsWriter(path)
      case None => IndexedTransactionsWriter()
    }
  }

}
