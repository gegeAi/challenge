package com.jliermann.parser

object PricesFields {

  val PRODUCT = 0
  val PRICE = 1
}

object Prices extends Mapper[Price] {

  import PricesFields._

  override def mapClass(obj: Price): String = {
    List(
      obj.product.toString, // necessary to infer other toString calls
      obj.price
    ).mkString(writeSep)
  }

  override protected def getObject(fields: Seq[String]): Price = {
    Price(fields(PRODUCT).toInt, fields(PRICE).toDouble)
  }
}

case class Price(product: Int, price: Double)
