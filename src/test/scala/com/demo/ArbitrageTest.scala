package com.demo

import com.demo.Currency._
import com.demo.TestBuilder._
import com.demo.exchange._
import org.mockito.ArgumentMatchersSugar
import org.mockito.scalatest.IdiomaticMockito
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class ArbitrageTest extends FeatureSpec with GivenWhenThen with IdiomaticMockito with ArgumentMatchersSugar with Matchers with TestDsl with Equalities {

  val exchangeConnector = mock[ExchangeConnector]
  implicit val oms      = new OMS(exchangeConnector)

  feature("When an arbitrage opportunity occurs the system has to execute the required orders to make money") {

    scenario("Triangular arbitrage") {
      Given("we have some money")
      updateAvailableFunds(10000.USD)

      And("the following market data")
      onMarketData(BTC / USD, asks = Seq(10.BTC at 7900.USD))
      onMarketData(ETH / BTC, asks = Seq(100.ETH at .03.BTC))
      onMarketData(ETH / USD, bids = Seq(50.ETH at 250.USD))

      Then("we send an order to buy 1.27.BTC at 7900.USD")
      exchangeConnector.executeOnExchange(order(1.27.BTC at 7900.USD, OurId("ourId1"), Side.Bid)) was called

      When("the market fills the first order")
      onMarketReport(1.27.BTC at 7900.USD, OurId("ourId1"), ExchangeId("exchangeId1"))

      Then("we send an order to buy 42.33.ETH")
      exchangeConnector.executeOnExchange(order(42.33.ETH at .03.BTC, OurId("ourId2"), Side.Bid)) was called

      When("the market fills the second order")
      onMarketReport(42.33.ETH at .03.BTC, OurId("ourId2"), ExchangeId("exchangeId2"))

      Then("we send an order to sell 42.33.ETH")
      exchangeConnector.executeOnExchange(order(42.33.ETH at 250.USD, OurId("ourId3"), Side.Ask)) was called

      When("the market fills the third order")
      onMarketReport(42.33.ETH at 250.USD, OurId("ourId3"), ExchangeId("exchangeId3"))

      Then("the balance is now bigger")
      oms.funds(USD) shouldBe 10582.5.USD
    }
  }
}
