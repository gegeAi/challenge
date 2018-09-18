package com.jliermann.parser

import org.scalatest.FlatSpec

class TransactionsSpec extends FlatSpec {

  "mapLine" should "return some transaction if no error occurs" in {

    val strTransaction = "1|20170514T233544CEST|abc|531|5"
    val classTransaction = Transaction(
      1,
      Transactions.fieldDateFormat.parse("20170514T223544+0100"),
      "abc",
      531,
      5)

    assert(Transactions.mapLine(strTransaction).contains(classTransaction))
  }

  "mapClass" should "convert a Transaction to a string" in {

    val strTransaction = "1|20170514T233544CEST|abc|531|5"
    val classTransaction = Transaction(
      1,
      Transactions.fieldDateFormat.parse("20170514T223544+0100"),
      "abc",
      531,
      5)

    assert(strTransaction == Transactions.mapClass(classTransaction))
  }

}
