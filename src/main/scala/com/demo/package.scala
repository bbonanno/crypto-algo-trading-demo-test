package com

import io.estatico.newtype.macros.newtype

package object demo {

  @newtype case class Currency(name: String) {
    def /(quote: Currency): CurrencyPair = CurrencyPair(this, quote)
  }
  object Currency {
    val BTC = Currency("BTC")
    val ETH = Currency("ETH")
    val USD = Currency("USD")
  }

  case class CurrencyPair(base: Currency, quote: Currency)

  case class Quantity(value: BigDecimal, currency: Currency) {
    def convert(at: Rate): Quantity = {
      if (currency == at.ccyPair.base) Quantity(value * at.value, at.ccyPair.quote)
      else Quantity(value / at.value, at.ccyPair.base)
    }
  }
  case class Rate(value: BigDecimal, ccyPair: CurrencyPair)

  case class PriceLevel(quantity: Quantity, rate: Rate)
  case class OrderBook(bids: Seq[PriceLevel], asks: Seq[PriceLevel])
}
