package com.jliermann.util

import java.io.{BufferedWriter, File, FileWriter}

import org.slf4j.{Logger, LoggerFactory}
import resource._

import scala.io.Source

object GenericFilesOperations {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def bufferedWriting(file: File, append: Boolean = true)(toWriteOperation: BufferedWriter => Unit): Unit = {

    file.getParentFile.mkdirs()

    if(!file.exists() && !file.createNewFile()) {
      throw ErrorOnFileCreation(file)
    } else {
      for {
        fw <- managed(new FileWriter(file, append))
        bw <- managed(new BufferedWriter(fw))
      } {
        toWriteOperation(bw)
      }
    }
  }

  def foreachReading(file: File)(toReadOperation: String => Unit): Unit = {

    if(file.exists) {

      for {
        br <- managed(Source.fromFile(file, "UTF-8"))
      } {
        br.getLines.foreach(toReadOperation)
      }
    } else {
      logger.debug(s"can't read file ${file.getAbsolutePath}. File doesn't exist")
    }
  }

  def reduceReading[T](file: File, acc: T)(seqOp: (T, String) => T, combOp: (T, T) => T): T = {

    if(file.exists) {

      val fileReadingO = for {
        br <- managed(Source.fromFile(file, "UTF-8"))
      } yield {
        br.getLines.aggregate(acc)(seqOp, combOp)
      }
      fileReadingO.opt match {
        case Some(fileReading) => fileReading
        case None => acc
      }

    } else {
      logger.debug(s"can't read file ${file.getAbsolutePath}. File doesn't exist")
      acc
    }
  }

  def deleteDirs(file: File): Unit = {
    if(file != null) {
      val subFiles = file.listFiles()
      if(subFiles != null) {
        subFiles.foreach(deleteDirs)
      }
      file.delete()
    }
  }

}

case class ErrorOnFileCreation(file: File) extends Exception(s"Can't create file <${file.getAbsolutePath}>")
