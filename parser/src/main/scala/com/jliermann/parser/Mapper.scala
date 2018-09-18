package com.jliermann.parser

import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

trait Mapper[T] {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  val readSep: String = "\\|"
  val writeSep = "|"

  def mapLine(line: String): Option[T] = {
    Try {
      val fields = line.split(readSep)
      getObject(fields)
    } match {
      case Success(transaction) => Some(transaction)
      case Failure(e) => {
        logger.warn(s"failed to map line <$line> due to error <${e.getMessage}>;\n skip.")
        None
      }
    }
  }
  def mapClass(obj: T): String

  protected def getObject(fields: Seq[String]): T

}
