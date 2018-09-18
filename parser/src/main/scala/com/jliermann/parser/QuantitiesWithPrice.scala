package com.jliermann.parser

import java.text.SimpleDateFormat
import java.util.Date

object QuantityWithPriceFields {
  val PRODUCT = 0
  val STORE = 1
  val REDUCED_TIMESTAMP = 2
  val QUANTITY = 3
  val TOTAL_PRICE = 4
}

object QuantitiesWithPrice extends Mapper[QuantityWithPrice] {

  import QuantityWithPriceFields._

  val parser = new SimpleDateFormat("yyyyMMdd")

  def sumIfDefined(qtyWtihPrice: QuantityWithPrice, maybeQuantity: Option[QuantityWithPrice]): QuantityWithPrice = {
    maybeQuantity match {
      case Some(quantity) => sum(qtyWtihPrice, quantity)
      case None => qtyWtihPrice
    }
  }

  def sum(qtyForQtyAndPrice: QuantityWithPrice, qtyForMore: QuantityWithPrice): QuantityWithPrice = {
    qtyForMore.copy(
      quantity = qtyForQtyAndPrice.quantity + qtyForMore.quantity,
      totalPrice = qtyForQtyAndPrice.totalPrice + qtyForMore.totalPrice)
  }

  def fromQuantityAndPrice(quantity: Transaction, price: Price): Option[QuantityWithPrice] = {

    if(quantity.product == price.product) {
      Some(
        QuantityWithPrice(
          quantity.product,
          quantity.store,
          quantity.timeStamp,
          quantity.quantity,
          quantity.quantity * price.price
        )
      )
    } else {
      //logger.warn(s"Can't join $quantity and $price. Product ids are differents")
      None
    }
  }

  def mapClass(obj: QuantityWithPrice, global: Boolean): String = {
    if(global) {
      List(
        obj.product,
        "GLOBAL",
        parser.format(obj.reducedTimeStamp),
        obj.quantity,
        obj.totalPrice
      ).mkString(writeSep)
    } else {
      mapClass(obj)
    }
  }

  override def mapClass(obj: QuantityWithPrice): String = {
    List(
      obj.product,
      obj.store,
      parser.format(obj.reducedTimeStamp),
      obj.quantity,
      obj.totalPrice
    ).mkString(writeSep)
  }

  override protected def getObject(fields: Seq[String]): QuantityWithPrice = {
    QuantityWithPrice(
      fields(PRODUCT).toInt,
      fields(STORE),
      parser.parse(fields(REDUCED_TIMESTAMP)),
      fields(QUANTITY).toInt,
      fields(TOTAL_PRICE).toDouble
    )
  }
}

case class QuantityWithPrice(product: Int,
                             store: String,
                             reducedTimeStamp: Date,
                             quantity: Int,
                             totalPrice: Double)

