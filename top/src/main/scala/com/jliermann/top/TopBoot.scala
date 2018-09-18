package com.jliermann.top

import java.util.{Calendar, Date}

object TopBoot extends App {

  val parser = new scopt.OptionParser[TopConfig]("ReferentialIndexer") {
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

    opt[String]('a', "aggregated-files").optional.maxOccurs(1).action { (x, c) =>
      c.copy(aggregationPath = Some(x))
    }.text("path to the files created by the indexer (expects a directory)")

    opt[String]('s', "save").optional.maxOccurs(1).action { (x, c) =>
      c.copy(savePath = Some(x))
    }.text("path where to save the files created (expects a directory)")
  }

  parser.parse(args, TopConfig()) match {
    case Some(config) ⇒
      val splitter: TopEngine = new TopEngine(
        config.from,
        config.to,
        config.computeWeeks,
        config.global,
        config.aggregationPath,
        config.savePath)
      splitter.run()

    case None ⇒
      parser.showUsage
      System.exit(2)

  }

}

case class TopConfig(from: Date = new Date(),
                     to: Date = new Date(),
                     computeWeeks: Boolean = false,
                     global: Boolean = false,
                     aggregationPath: Option[String] = None,
                     savePath: Option[String] = None)
