package com.demo
package exchange

case class Order(quantity: Quantity, rate: Rate, orderId: String, side: Side)

trait ExchangeConnector {
  def executeOnExchange(order: Order): Unit
}
