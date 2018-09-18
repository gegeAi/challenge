package com.jliermann.aggregator

import java.util.{Calendar, Date}

object AggregatorBoot extends App {

  val parser = new scopt.OptionParser[AggregatorConfig]("ReferentialIndexer") {
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

    opt[String]('i', "indexed-files").optional.maxOccurs(1).action { (x, c) =>
      c.copy(indexedTransactionsPath = Some(x))
    }.text("path to the files created by the indexer (expects a directory)")

    opt[String]('p', "prices-files").optional.maxOccurs(1).action { (x, c) =>
      c.copy(pricesPath = Some(x))
    }.text("path to the files representing the prices of products (expects a directory)")

    opt[String]('s', "save").optional.maxOccurs(1).action { (x, c) =>
      c.copy(aggregationSavePath = Some(x))
    }.text("path where to save the files created (expects a directory")
  }

  parser.parse(args, AggregatorConfig()) match {
    case Some(config) =>
      val engine: AggregatorEngine = new AggregatorEngine(
        config.from,
        config.to,
        config.computeWeeks,
        config.indexedTransactionsPath,
        config.pricesPath,
        config.aggregationSavePath)
      engine.run()
    case None =>
      parser.showUsage
      System.exit(2)
  }

}

case class AggregatorConfig(from: Date = new Date(),
                            to: Date = new Date(),
                            computeWeeks: Boolean = false,
                            indexedTransactionsPath: Option[String] = None,
                            pricesPath: Option[String] = None,
                            aggregationSavePath: Option[String] = None)
