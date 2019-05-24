package com.demo

package object exchange {

  case class ClackatronOrderId(id: String)
  case class ExchangeOrderId(id: String)

  sealed trait Side
  object Side {
    case object Bid extends Side
    case object Ask extends Side
  }

  sealed trait Message

  case class MarketData(currencyPair: CurrencyPair, bids: Seq[PriceLevel], asks: Seq[PriceLevel]) extends Message

  case class MarketReport(orderId: ClackatronOrderId, exchangeId: ExchangeOrderId, filledBase: Quantity, rate: Price) extends Message

  case class AvailableFunds(funds: Seq[Quantity]) extends Message

}
