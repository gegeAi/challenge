package com.jliermann.parser

import org.scalatest.FlatSpec

class PricesSpec extends FlatSpec {

  "mapLine" should "return some Price if no error occurs" in {

    val strPrice = "1|3.14159265"
    val classPrice = Price(1, 3.14159265)

    assert(Prices.mapLine(strPrice).contains(classPrice))
  }

  "mapClass" should "convert a Price to a string" in {

    val strPrice = "1|3.14159265"
    val classPrice = Price(1, 3.14159265)
    assert(strPrice == Prices.mapClass(classPrice))
  }

}
