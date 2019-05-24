package com.demo
package exchange

case class Order(quantity: Quantity, price: Price, orderId: ClackatronOrderId, side: Side)

trait ExchangeConnector {
  def executeOnExchange(order: Order): Unit
}
