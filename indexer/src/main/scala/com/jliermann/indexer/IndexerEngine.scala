package com.jliermann.indexer

import java.util.Date

import com.jliermann.parser.Transaction
import com.jliermann.util.DateRange

class IndexerEngine(from: Date,
                    to: Date,
                    computeWeeks: Boolean,
                    transactionsFileSources: Option[String],
                    indexedSavePath: Option[String]) {



  def run(): Unit = {
    val (accurateFrom: Date, accurateTo: Date) = if(computeWeeks) DateRange.adjustToWeeks(from, to) else (from, to)

    val reader = TransactionsReader(transactionsFileSources)
    val writer = IndexedTransactionsWriter(indexedSavePath)

    DateRange(accurateFrom, accurateTo).iterator().foreach(readFile(reader, writer))
  }

  private def indexLine(writer: IndexedTransactionsWriter)(line: Option[Transaction]): Unit = {
    writer.addLineIfExists(line)
  }

  private def readFile(reader: TransactionsReader, writer: IndexedTransactionsWriter)(date: Date): Unit = {
    reader.iterateOnTransactions(date)(indexLine(writer))

  }


}

