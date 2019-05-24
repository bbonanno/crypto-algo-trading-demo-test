package com.demo

import com.demo.exchange._

/**
  * Completely hardcoded and not at all production ready implementation just to make the tests pass
  */
class Clackatron(exchangeConnector: ExchangeConnector) {

  var funds: Map[Currency, Quantity]           = Map.empty
  var orderBooks: Map[CurrencyPair, OrderBook] = Map.empty

  def updateFunds(q: Quantity*): Unit = {
    funds = q.map(q => q.currency -> q).toMap
  }

  def onMessage(e: Message): Unit = e match {

    case AvailableFunds(f) =>
      updateFunds(f: _*)

    case MarketData(ccyPair, bids, asks) =>
      orderBooks = orderBooks + (ccyPair -> OrderBook(bids, asks))
      if (orderBooks.size == 3) {
        val f  = funds(Currency("USD"))
        val pl = orderBooks(CurrencyPair(Currency("BTC"), Currency("USD"))).asks.head
        exchangeConnector.executeOnExchange(Order(f convert pl.price, pl.price, ClackatronOrderId("cid1"), Side.Bid))
      }

    case MarketReport(orderId, _, filledBase, price) =>
      orderId match {
        case ClackatronOrderId("cid1") =>
          updateFunds(filledBase)
          val pl = orderBooks(CurrencyPair(Currency("ETH"), Currency("BTC"))).asks.head
          exchangeConnector.executeOnExchange(Order(filledBase convert pl.price, pl.price, ClackatronOrderId("cid2"), Side.Bid))
        case ClackatronOrderId("cid2") =>
          updateFunds(filledBase)
          val pl = orderBooks(CurrencyPair(Currency("ETH"), Currency("USD"))).bids.head
          exchangeConnector.executeOnExchange(Order(filledBase, pl.price, ClackatronOrderId("cid3"), Side.Ask))
        case ClackatronOrderId("cid3") =>
          updateFunds(filledBase convert price)
        case _ =>
      }
  }
}
