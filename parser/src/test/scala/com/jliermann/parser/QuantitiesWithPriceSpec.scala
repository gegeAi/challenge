package com.jliermann.parser

import java.text.SimpleDateFormat
import java.util.Date

import org.scalatest.FlatSpec

class QuantitiesWithPriceSpec extends FlatSpec {

  val refDate: Date = new SimpleDateFormat("yyyyMMdd").parse("20170514")

  "mapLine" should "return some QuantityWithPrice if no error occurs" in {

    val strQtyPrice: String = "0|abc|20170514|6|3.14159265"
    val classQtyPrice: QuantityWithPrice = QuantityWithPrice(0, "abc", refDate, 6, 3.14159265)

    assert(QuantitiesWithPrice.mapLine(strQtyPrice).contains(classQtyPrice))
  }

  "mapClass" should "convert a QuantityWithPrice to a string" in {

    val strQtyPrice: String = "0|abc|20170514|6|3.14159265"
    val classQtyPrice: QuantityWithPrice = QuantityWithPrice(0, "abc", refDate, 6, 3.14159265)

    assert(strQtyPrice == QuantitiesWithPrice.mapClass(classQtyPrice))
  }

  "fromQuantityAndPrice" should "return some QuantityWithPrice if quantity and price product's id match" in {

    val quantity: Transaction = Transaction(1, refDate, "abc", 531, 5)
    val price: Price = Price(531, 3.14159265)
    val qtyPrice: QuantityWithPrice = QuantityWithPrice(531, "abc", refDate, 5, 5*3.14159265)

    assert(QuantitiesWithPrice.fromQuantityAndPrice(quantity, price).contains(qtyPrice))

  }

  it should "return None if product's ids don't match" in {

    val quantity: Transaction = Transaction(1, refDate, "abc", 531, 5)
    val price: Price = Price(6, 3.14159265)

    assert(QuantitiesWithPrice.fromQuantityAndPrice(quantity, price).isEmpty)
  }

}
