package com.jliermann.parser

import java.text.SimpleDateFormat
import java.util.Date

object TransactionFields {
  val ID = 0
  val TIMESTAMP = 1
  val STORE = 2
  val PRODUCT = 3
  val QUANTITY = 4
}

object Transactions extends Mapper[Transaction] {

  import TransactionFields._

  val fieldDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssz")

  def sum(transactionForQty: Transaction, transactionForMore: Transaction): Transaction = {
    transactionForMore.copy(quantity = transactionForMore.quantity + transactionForQty.quantity)
  }

  def sumIfDefined(transactionForQty: Transaction, transactionForMore: Option[Transaction]): Transaction = {

    transactionForMore match {
      case Some(transaction) => sum(transactionForQty, transaction)
      case None => transactionForQty
    }
  }

  override def mapClass(transaction: Transaction): String = {
    Array(
      transaction.id,
      fieldDateFormat.format(transaction.timeStamp),
      transaction.store,
      transaction.product,
      transaction.quantity
    ).mkString(writeSep)
  }

  override protected def getObject(fields: Seq[String]): Transaction = {
    Transaction(
      fields(ID).toInt,
      fieldDateFormat.parse(fields(TIMESTAMP)),
      fields(STORE),
      fields(PRODUCT).toInt,
      fields(QUANTITY).toInt)
  }

}

case class Transaction(id: Int, timeStamp: Date, store: String, product: Int, quantity: Int)


