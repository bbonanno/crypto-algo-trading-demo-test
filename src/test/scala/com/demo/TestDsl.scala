package com.demo

import com.demo.exchange.{AvailableFunds, ExchangeOrderId, MarketData, MarketReport, OmsOrderId, Order}
import org.scalactic.Equality

trait TestDsl {

  implicit val orderEq: Equality[Order] = (a: Order, b: Any) => b match {
    case Order(quantity, price, orderId, side, _) =>
      a.quantity == quantity && a.price == price && a.orderId == orderId && a.side == side
  }

  implicit class NumberOps(d: Double) {
    def BTC: Quantity = Quantity(d, Currency.BTC)
    def ETH: Quantity = Quantity(d, Currency.ETH)
    def USD: Quantity = Quantity(d, Currency.USD)
  }

  implicit class QuantityOps(q: Quantity) {
    def at(price: Quantity): (Quantity, Price) = (q, Price(price.value, q.currency / price.currency))
  }

  def onMarketData(currencyPair: CurrencyPair, bids: Seq[PriceLevel] = Seq.empty, asks: Seq[PriceLevel] = Seq.empty)(implicit oms: OMS): Unit =
    oms onMessage MarketData(currencyPair, bids, asks)

  def onMarketReport(orderId: OmsOrderId, exchangeId: ExchangeOrderId, t: (Quantity, Price))(implicit oms: OMS): Unit =
    oms onMessage MarketReport(orderId, exchangeId, t._1, t._2)

  def setAvailableFunds(funds: Quantity*)(implicit oms: OMS): Unit =
    oms onMessage AvailableFunds(funds)

}
