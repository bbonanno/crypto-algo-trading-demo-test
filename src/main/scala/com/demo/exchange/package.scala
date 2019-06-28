package com.demo

import io.estatico.newtype.macros.newtype

package object exchange {

  @newtype case class BookVersion(id: Int)
  @newtype(unapply = true) case class OmsOrderId(id: Int)
  @newtype case class ExchangeOrderId(id: Int)

  sealed trait Side
  object Side {
    case object Bid extends Side
    case object Ask extends Side
  }

  sealed trait Message

  case class MarketData(currencyPair: CurrencyPair, bids: Seq[PriceLevel], asks: Seq[PriceLevel]) extends Message

  case class MarketReport(orderId: OmsOrderId, exchangeId: ExchangeOrderId, filledBase: Quantity, price: Price) extends Message

  case class AvailableFunds(funds: Seq[Quantity]) extends Message

}
