package com.jliermann.root

import java.io.File

import com.jliermann.parser.QuantitiesWithPrice
import com.jliermann.util.GenericFilesOperations
import org.scalatest.FlatSpec

class BootSpec extends FlatSpec {


  "main" should "create two top files for each store" in {

    Boot.main(Array("--date", "2017-05-14", "--in", "./root/resources", "--out", "/tmp/out"))

    val resources: File = new File("./root/resources")
    val storeNumber: Int = resources.listFiles.length - 1

    val outDirectory = new File("/tmp/out")

    assert(outDirectory.listFiles.length == storeNumber * 2)

    GenericFilesOperations.deleteDirs(outDirectory)

  }

  it should "generate files of 100 lines long" in {

    Boot.main(Array("--date", "2017-05-14", "--in", "./root/resources", "--out", "/tmp/out"))

    val outDirectory = new File("/tmp/out")

    val fileLength = GenericFilesOperations.reduceReading(outDirectory.listFiles.head, 0)(
      (acc, line) => QuantitiesWithPrice.mapLine(line) match {
        case Some(_) => acc + 1
        case None => acc
      },
      (acc1, acc2) => acc1 + acc2
    )

    assert(fileLength == 100)

    GenericFilesOperations.deleteDirs(outDirectory)

  }
}
