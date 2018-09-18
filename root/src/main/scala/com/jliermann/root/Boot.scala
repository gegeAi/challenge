package com.jliermann.root

import java.io.File
import java.util.{Calendar, Date}

import com.jliermann.aggregator.AggregatorEngine
import com.jliermann.indexer.IndexerEngine
import com.jliermann.top.TopEngine
import com.jliermann.util.GenericFilesOperations

object Boot extends App {

  val parser = new scopt.OptionParser[BootConfig]("ReferentialIndexer") {
    help("help").text("prints this usage text")
    opt[Calendar]('f', "from").optional().maxOccurs(1).action { (x, c) ⇒
      c.copy(from = x.getTime)
    }.text("begin date to operate on a range of days")

    opt[Calendar]('t', "to").optional().maxOccurs(1).action { (x, c) ⇒
      c.copy(to = x.getTime)
    }.text("end date to operate on a range of days")

    opt[Calendar]('d', "date").optional().maxOccurs(1).action { (x, c) ⇒
      c.copy(from = x.getTime, to = x.getTime)
    }.text("date to operate on a unique day. By default, --date now")

    opt[Unit]('w', "weeks").optional.maxOccurs(1).action { (_, c) ⇒
      c.copy(computeWeeks = true)
    }.text(
      """Compute weekly statistics. Adjust the date range to corresponds
        |to the smallest range including every date in the prior interval, but beginning on
        |a Monday end ending on a Sunday. false by default
      """.stripMargin)

    opt[Unit]('g', "global").optional.maxOccurs(1).action { (_, c) ⇒
      c.copy(global = true)
    }.text(
      """Compute statistics aggregated for every stores alltogether. false by default
      """.stripMargin)

    opt[String]('i', "in").optional.maxOccurs(1).action { (x, c) =>
      c.copy(transactionsPath = Some(x))
    }.text("path to the files created by the indexer (expects a directory)")

    opt[String]('o', "out").optional.maxOccurs(1).action { (x, c) =>
      c.copy(savePath = Some(x))
    }.text("path to the files representing the prices of products (expects a directory)")

  }

  parser.parse(args, BootConfig()) match {
    case Some(config) =>
      val indexer = new IndexerEngine(config.to, config.from, config.computeWeeks, config.transactionsPath, None)
      val aggregator = new AggregatorEngine(config.from, config.to, config.computeWeeks, None, config.transactionsPath, None)
      val top = new TopEngine(config.from, config.to, config.computeWeeks, config.global, None, config.savePath)
      indexer.run()
      aggregator.run()
      top.run()
      GenericFilesOperations.deleteDirs(new File("/tmp/transactions/indexed-transactions"))
      GenericFilesOperations.deleteDirs(new File("/tmp/transactions/aggregated-transactions"))
    case None =>
      parser.showUsage()
      System.exit(2)
  }
}


case class BootConfig(from: Date = new Date(),
                            to: Date = new Date(),
                            computeWeeks: Boolean = false,
                            global: Boolean = false,
                            transactionsPath: Option[String] = None,
                            savePath: Option[String] = None)
