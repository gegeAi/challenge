package com.jliermann.indexer

import java.io.File
import java.nio.file.Paths
import java.util.{Calendar, Date}


object IndexerBoot extends App {

  val parser = new scopt.OptionParser[IndexerConfig]("ReferentialIndexer") {
    help("help").text("prints this usage text")
    opt[Calendar]('f', "from").optional().maxOccurs(1).action { (x, c) ⇒
     c.copy(from = x.getTime)
    }.text("begin date to split multiple files")

    opt[Calendar]('t', "to").optional().maxOccurs(1).action { (x, c) ⇒
      c.copy(to = x.getTime)
    }.text("end date to split multiple files")

    opt[Calendar]('d', "date").optional().maxOccurs(1).action { (x, c) ⇒
    c.copy(from = x.getTime, to = x.getTime)
    }.text("date to select a unique transaction file. By default, --date now")

    opt[Unit]('w', "weeks").optional.maxOccurs(1).action { (_, c) ⇒
      c.copy(computeWeeks = true)
    }.text(
      """Compute weekly statistics. Adjust the date range to corresponds
        |to the smallest range including every date in the prior interval, but beginning on
        |a Monday end ending on a Sunday. false by default
      """.stripMargin)

    opt[String]('p', "--path").optional.maxOccurs(1).action { (x, c) ⇒
      c.copy(transactionsDatas = Some(x))
    }.text("Where to get the original datas (path to directory expected)")

    opt[String]('s', "--save").optional.maxOccurs(1).action { (x, c) ⇒
      c.copy(indexedDatasSavePath = Some(x))
    }.text("Where to save the indexed datas (path to directory expected)")
  }

  parser.parse(args, IndexerConfig()) match {
    case Some(config) ⇒
      val splitter: IndexerEngine = new IndexerEngine(
        config.from,
        config.to,
        config.computeWeeks,
        config.transactionsDatas,
        config.indexedDatasSavePath)

        splitter.run()

    case None ⇒
      parser.showUsage
      System.exit(2)

  }

}

case class IndexerConfig(from: Date = new Date(),
                         to: Date = new Date(),
                         computeWeeks: Boolean = false,
                         transactionsDatas: Option[String] = None,
                         indexedDatasSavePath: Option[String] = None)

