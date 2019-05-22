package com.demo

import com.demo.exchange._

trait TestDsl {

  implicit class NumberOps[N](i: N)(implicit n: Numeric[N]) {
    def USD: Quantity = Quantity(n.toDouble(i), Currency.USD)
    def ETH: Quantity = Quantity(n.toDouble(i), Currency.ETH)
    def BTC: Quantity = Quantity(n.toDouble(i), Currency.BTC)
  }

  def updateAvailableFunds(quantities: Quantity*)(implicit oms: OMS) = oms onMessage AvailableFunds(quantities)

  def onMarketData(currencyPair: CurrencyPair, bids: Seq[PriceLevel] = Seq.empty, asks: Seq[PriceLevel] = Seq.empty)(implicit oms: OMS) =
    oms onMessage MarketData(currencyPair, bids, asks)

  def onMarketReport(filledBase: Quantity, rate: Rate, orderId: String, exchangeId: String)(implicit oms: OMS) =
    oms onMessage MarketReport(orderId, exchangeId, filledBase, rate)
}
