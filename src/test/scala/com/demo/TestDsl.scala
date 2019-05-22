package com.demo

import com.demo.exchange._

trait TestDsl {

  def updateAvailableFunds(quantities: Quantity*)(implicit oms: OMS) = oms onMessage AvailableFunds(quantities)

  def onMarketData(currencyPair: CurrencyPair, bids: Seq[PriceLevel] = Seq.empty, asks: Seq[PriceLevel] = Seq.empty)(implicit oms: OMS) =
    oms onMessage MarketData(currencyPair, bids, asks)

  def onMarketReport(filledBase: Quantity, rate: Rate, orderId: String, exchangeId: String)(implicit oms: OMS) =
    oms onMessage MarketReport(orderId, exchangeId, filledBase, rate)
}
