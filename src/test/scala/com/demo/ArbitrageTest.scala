package com.demo

import com.demo.Currency._
import com.demo.exchange._
import org.mockito.scalatest.MockitoSugar
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}
import TestBuilder._

class ArbitrageTest extends FeatureSpec with GivenWhenThen with MockitoSugar with Matchers with TestDsl {

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
      verify(exchangeConnector).executeOnExchange(order(1.27.BTC at 7900.USD, "ourId1", Side.Bid))

      When("the market fills the first order")
      onMarketReport(1.27.BTC at 7900.USD, "exchangeId1", "ourId1")

      Then("we send an order to buy 42.33.ETH")
      verify(exchangeConnector).executeOnExchange(order(42.33.ETH at .03.BTC, "ourId2", Side.Bid))

      When("the market fills the second order")
      onMarketReport(42.33.ETH at .03.BTC, "exchangeId2", "ourId2")

      Then("we send an order to sell 42.33.ETH")
      verify(exchangeConnector).executeOnExchange(order(42.33.ETH at 250.USD, "ourId3", Side.Ask))

      When("the market fills the third order")
      onMarketReport(42.33.ETH at 250.USD, "exchangeId3", "ourId3")

      Then("the balance is now bigger")
      oms.funds(USD) shouldBe 10582.5.USD
    }
  }
}
