package com.demo

import com.demo.TestBuilder._
import com.demo.exchange._
import org.scalactic.Equality
import org.scalactic.TripleEquals._

import scala.math.BigDecimal.RoundingMode

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

  def onMarketReport(qr: (Quantity, Rate), orderId: OurId, exchangeId: ExchangeId)(implicit oms: OMS) =
    oms onMessage MarketReport(orderId, exchangeId, qr._1, qr._2)
}

object TestBuilder {

  def order(qr: (Quantity, Rate), orderId: OurId, side: Side): Order = Order(qr._1, qr._2, orderId, side)

  def priceLevel(qr: (Quantity, Rate)): PriceLevel = PriceLevel(qr._1, qr._2)
}

trait Equalities {
  implicit val RoundedEquality: Equality[BigDecimal] = (a: BigDecimal, b: Any) =>
    b match {
      case bd: BigDecimal => a.setScale(2, RoundingMode.HALF_EVEN) == bd.setScale(2, RoundingMode.HALF_EVEN)
      case _              => false
  }

  implicit val QuantityEquality: Equality[Quantity] = (a: Quantity, b: Any) =>
    b match {
      case Quantity(v, ccy) => a.value === v && a.currency == ccy
      case _                => false
  }

  implicit val RateEquality: Equality[Rate] = (a: Rate, b: Any) =>
    b match {
      case Rate(v, ccyPair) => a.value === v && a.ccyPair == ccyPair
      case _                => false
  }

  implicit val OrderEquality: Equality[Order] = (a: Order, b: Any) =>
    b match {
      case Order(quantity, rate, id, side) => a.quantity === quantity && a.rate === rate && a.orderId == id && a.side == side
      case _                               => false
  }

}
