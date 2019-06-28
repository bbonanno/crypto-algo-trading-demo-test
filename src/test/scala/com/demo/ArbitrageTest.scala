package com.demo

import com.demo.Currency._
import com.demo.exchange._
import org.mockito.scalatest.IdiomaticMockito
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class ArbitrageTest extends FeatureSpec with GivenWhenThen with IdiomaticMockito with Matchers with TestBuilder with TestDsl {

  val exchangeConnector = mock[ExchangeConnector]
  implicit val oms      = new OMS(exchangeConnector)

  feature("When an arbitrage opportunity occurs the system has to execute the required orders to make money") {

    scenario("Triangular arbitrage") {
      And("we have some money")
      setAvailableFunds(10000.USD)

      And("the following market data")
      onMarketData(BTC / USD, asks = Seq(priceLevel(10.BTC at 7900.USD)))
      onMarketData(ETH / BTC, asks = Seq(priceLevel(100.ETH at .03.BTC)))
      onMarketData(ETH / USD, bids = Seq(priceLevel(50.ETH at 250.USD)))

      Then("we send an order to buy 1.27.BTC")
      exchangeConnector.executeOnExchange(order(1.27.BTC at 7900.USD, OmsOrderId(1), Side.Bid)) was called

      When("the market fills the first order")
      onMarketReport(OmsOrderId(1), ExchangeOrderId(100), 1.27.BTC at 7900.USD)

      Then("we send an order to buy 42.33.ETH")
      exchangeConnector.executeOnExchange(order(42.33.ETH at .03.BTC, OmsOrderId(2), Side.Bid)) was called

      When("the market fills the second order")
      onMarketReport(OmsOrderId(2), ExchangeOrderId(200), 42.33.ETH at .03.BTC)

      Then("we send an order to sell 42.33.ETH")
      exchangeConnector.executeOnExchange(order(42.33.ETH at 250.USD, OmsOrderId(3), Side.Ask)) was called

      When("the market fills the third order")
      onMarketReport(OmsOrderId(3), ExchangeOrderId(300), 42.33.ETH at 250.USD)

      Then("the balance is now bigger")
      oms.funds(USD) shouldBe 10582.5.USD
    }
  }
}
