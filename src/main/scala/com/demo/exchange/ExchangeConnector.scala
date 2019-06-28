package com.demo
package exchange

case class Order(quantity: Quantity, price: Price, orderId: OmsOrderId, side: Side, bookVersion: BookVersion)

trait ExchangeConnector {
  def executeOnExchange(order: Order): Unit
}
