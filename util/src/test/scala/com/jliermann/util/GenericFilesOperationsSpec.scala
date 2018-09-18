package com.jliermann.util

import java.io._
import java.nio.file.{Path, Paths}

import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import resource.managed

class GenericFilesOperationsSpec extends FlatSpec with MockitoSugar {


  val mockLocation: Path = Paths.get("/tmp/transactions-test")

  "bufferedWriting" should "throw an ErrorOnFileCreation if file can't be created" in {

    val file: File = mock[File]
    val parentFile: File = mock[File]

    when(file.getParentFile).thenReturn(parentFile)
    when(file.exists).thenReturn(false)
    when(file.createNewFile()).thenReturn(false)

    def executedIfFileCreated = (_: BufferedWriter) => System.out.println("dummy")

    assertThrows[ErrorOnFileCreation](GenericFilesOperations.bufferedWriting(file)(executedIfFileCreated))

  }

  it should "create every subdirectories and file if they don't exist" in {
    if(mockLocation.toFile.exists()) {
      GenericFilesOperations.deleteDirs(mockLocation.toFile)
    }
    val file = Paths.get(mockLocation.toString, "totally/awesome/file.data").toFile

    def executedIfFileCreated = (_: BufferedWriter) => System.out.println("dummy")

    GenericFilesOperations.bufferedWriting(file)(executedIfFileCreated)

    assert(file.exists())
    GenericFilesOperations.deleteDirs(mockLocation.toFile)
  }

  it should "append to a file by default" in {

    if(mockLocation.toFile.exists()) {
      GenericFilesOperations.deleteDirs(mockLocation.toFile)
    }
    val file = Paths.get(mockLocation.toString, "totally/awesome/file.data").toFile

    GenericFilesOperations.bufferedWriting(file)(br => br.write("first"))
    GenericFilesOperations.bufferedWriting(file)(br => br.write("second"))

    for {
      fileReader <- managed(new FileReader(file))
      reader <- managed(new BufferedReader(fileReader))
    } {
      assert(reader.readLine() == "firstsecond")
    }

    file.delete
    GenericFilesOperations.deleteDirs(mockLocation.toFile)
  }

  it should "write over if specified" in {

    if(mockLocation.toFile.exists()) {
      GenericFilesOperations.deleteDirs(mockLocation.toFile)
    }
    val file = Paths.get(mockLocation.toString, "totally/awesome/file.data").toFile

    GenericFilesOperations.bufferedWriting(file, append = false)(br => br.write("first"))
    GenericFilesOperations.bufferedWriting(file, append = false)(br => br.write("second"))

    for {
      fileReader <- managed(new FileReader(file))
      reader <- managed(new BufferedReader(fileReader))
    } {
      assert(reader.readLine() == "second")
    }

    file.delete
    GenericFilesOperations.deleteDirs(mockLocation.toFile)
  }

  "foreachReading" should "do the required operation for each line of the file" in {

    val fileLines = List("first", "second")

    if(mockLocation.toFile.exists()) {
      GenericFilesOperations.deleteDirs(mockLocation.toFile)
    }
    val file = Paths.get(mockLocation.toString, "totally/awesome/file.data").toFile

    file.getParentFile.mkdirs()
    file.createNewFile()

    for {
      fileWriter <- managed(new FileWriter(file))
      bw <- managed(new BufferedWriter(fileWriter))
    } {
      fileLines.foreach(line => {
        bw.write(line)
        bw.newLine()
      })
    }

    GenericFilesOperations.foreachReading(file)(line => assert(fileLines.contains(line)))
    GenericFilesOperations.deleteDirs(mockLocation.toFile)
  }

  "reduceReading" should "do the required aggregation operation for each line of the file" in {

    val fileLines = List("first", "second")

    if(mockLocation.toFile.exists()) {
      GenericFilesOperations.deleteDirs(mockLocation.toFile)
    }
    val file = Paths.get(mockLocation.toString, "totally/awesome/file.data").toFile

    file.getParentFile.mkdirs()
    file.createNewFile()

    for {
      fileWriter <- managed(new FileWriter(file))
      bw <- managed(new BufferedWriter(fileWriter))
    } {
      fileLines.foreach(line => {
        bw.write(line)
        bw.newLine()
      })
    }

    val concat: String = GenericFilesOperations.reduceReading(file, "")(
      (acc, line) => acc + "space" + line,
      (acc1, acc2) => acc1 + "space" + acc2
    )

    assert(concat.contains("first") && concat.contains("second"))

    GenericFilesOperations.deleteDirs(mockLocation.toFile)
  }

}
