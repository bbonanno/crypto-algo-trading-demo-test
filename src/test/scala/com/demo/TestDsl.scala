package com.demo

import com.demo.exchange._
import TestBuilder._

trait TestDsl {

  implicit class NumberOps[N](i: N)(implicit n: Numeric[N]) {
    def USD: Quantity = Quantity(n.toDouble(i), Currency.USD)
    def ETH: Quantity = Quantity(n.toDouble(i), Currency.ETH)
    def BTC: Quantity = Quantity(n.toDouble(i), Currency.BTC)
  }

  implicit class QuantityOps(q: Quantity) {
    def at(r: Quantity): (Quantity, Rate) = (q, Rate(r.value, q.currency / r.currency))
  }

  def updateAvailableFunds(quantities: Quantity*)(implicit oms: OMS) = oms onMessage AvailableFunds(quantities)

  def onMarketData(currencyPair: CurrencyPair, bids: Seq[(Quantity, Rate)] = Seq.empty, asks: Seq[(Quantity, Rate)] = Seq.empty)(implicit oms: OMS) =
    oms onMessage MarketData(currencyPair, bids.map(priceLevel), asks.map(priceLevel))

  def onMarketReport(qr: (Quantity, Rate), orderId: String, exchangeId: String)(implicit oms: OMS) =
    oms onMessage MarketReport(orderId, exchangeId, qr._1, qr._2)
}

object TestBuilder {

  def order(qr: (Quantity, Rate), orderId: String, side: Side): Order = Order(qr._1, qr._2, orderId, side)

  def priceLevel(qr: (Quantity, Rate)): PriceLevel = PriceLevel(qr._1, qr._2)
}
