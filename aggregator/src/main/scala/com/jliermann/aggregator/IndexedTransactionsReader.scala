package com.jliermann.aggregator

import java.io.File
import java.nio.file.Paths
import java.util.Date

import com.jliermann.indexer.IndexedTransactionsWriter
import com.jliermann.parser.{Transaction, Transactions}
import com.jliermann.util.GenericFilesOperations

class IndexedTransactionsReader(readPath: String) {

  val writerForPathGetting = IndexedTransactionsWriter(readPath)

  def readIndexedTransaction(date: Date, store: String, product: Int): Transaction = {

    val file: File = writerForPathGetting.getSaveFilePath(date, store, product).toFile
    GenericFilesOperations.reduceReading(file, Transaction(0, new Date(), "", 0, 0))(
      (acc, line) => Transactions.sumIfDefined(acc, Transactions.mapLine(line)),
      Transactions.sum
    )
  }


  def listStores(date: Date): Iterator[String] = {
    val dateIndex: File = Paths.get(
      IndexedTransactionsWriter.temporaryFilesPath,
      IndexedTransactionsWriter.saveDateFormat.format(date)
    ).toFile

    dateIndex.mkdirs()

    dateIndex
      .listFiles
      .map(_.getName).iterator
  }

}

object IndexedTransactionsReader {

  def apply(path: String = IndexedTransactionsWriter.temporaryFilesPath): IndexedTransactionsReader = {
    new IndexedTransactionsReader(path)
  }

  def apply(maybePath: Option[String]): IndexedTransactionsReader = {
    maybePath match {
      case Some(path) => IndexedTransactionsReader(path)
      case None => IndexedTransactionsReader()
    }
  }
}
