package com.jliermann.top

import java.util.Date

import com.jliermann.parser.QuantityWithPrice
import org.scalatest.FlatSpec

class TopComputerSpec extends FlatSpec {

  "addLine" should "add a line to the buffers if it is one of the 100est higher values, depending on the buffer" in {

    val quantities: Seq[QuantityWithPrice] = (1 to 200).map(
      qtyOrVal => QuantityWithPrice(0, "abc", new Date(), qtyOrVal, 200 - qtyOrVal))

    val finalBufferQty: Seq[QuantityWithPrice] = quantities.sortBy(_.quantity).takeRight(100)
    val finalBufferPrice: Seq[QuantityWithPrice] = quantities.sortBy(_.totalPrice).takeRight(100)

    val computer = TopComputer()

    quantities.foreach(qtyWithPrice => computer.addLine(qtyWithPrice))

    assert(computer.bufferPrices == finalBufferPrice)
    assert(computer.bufferQty == finalBufferQty)

  }
}
