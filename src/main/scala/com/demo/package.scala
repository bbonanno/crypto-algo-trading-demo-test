package com

import scala.math.BigDecimal.RoundingMode

package object demo {

  case class Currency(name: String) {
    def /(other: Currency): CurrencyPair = CurrencyPair(this, other)
  }
  object Currency {
    val BTC = Currency("BTC")
    val ETH = Currency("ETH")
    val USD = Currency("USD")
  }

  case class CurrencyPair(base: Currency, quote: Currency)

  case class Quantity(value: BigDecimal, currency: Currency) {
    def convert(at: Price): Quantity = {
      if (currency == at.ccyPair.base) Quantity(value * at.value setScale(2, RoundingMode.HALF_EVEN), at.ccyPair.quote)
      else Quantity(value / at.value setScale(2, RoundingMode.HALF_EVEN), at.ccyPair.base)
    }
  }
  case class Price(value: BigDecimal, ccyPair: CurrencyPair)

  case class PriceLevel(quantity: Quantity, price: Price)
  case class OrderBook(bids: Seq[PriceLevel], asks: Seq[PriceLevel])
}
