package com.demo

import com.demo.exchange._

trait TestBuilder {

  def order(t: (Quantity, Price), orderId: OmsOrderId, side: Side, bookVersion: BookVersion = BookVersion(1)): Order =
    Order(t._1, t._2, orderId, side, bookVersion)

  def priceLevel(t: (Quantity, Price)): PriceLevel =
    PriceLevel(t._1, t._2)
}
