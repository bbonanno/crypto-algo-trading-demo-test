package com.demo

import com.demo.Currency._
import com.demo.exchange._
import org.mockito.scalatest.IdiomaticMockito
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class ArbitrageTest extends FeatureSpec with GivenWhenThen with IdiomaticMockito with Matchers {

  val exchangeConnector = mock[ExchangeConnector]
  val oms               = new OMS(exchangeConnector)

  feature("When an arbitrage opportunity occurs the system has to execute the required orders to make money") {

    scenario("Triangular arbitrage") {
      Given("we have some money")
      oms onMessage AvailableFunds(Seq(Quantity(10000, USD)))

      And("the following market data")
      oms onMessage MarketData(
        CurrencyPair(BTC, USD),
        Seq(PriceLevel(Quantity(10, BTC), Rate(7899, CurrencyPair(BTC, USD)))),
        Seq(PriceLevel(Quantity(10, BTC), Rate(7900, CurrencyPair(BTC, USD))))
      )
      oms onMessage MarketData(
        CurrencyPair(ETH, BTC),
        Seq(PriceLevel(Quantity(100, ETH), Rate(.0299, CurrencyPair(ETH, BTC)))),
        Seq(PriceLevel(Quantity(100, ETH), Rate(.03, CurrencyPair(ETH, BTC))))
      )
      oms onMessage MarketData(
        CurrencyPair(ETH, USD),
        Seq(PriceLevel(Quantity(50, ETH), Rate(250, CurrencyPair(ETH, USD)))),
        Seq(PriceLevel(Quantity(50, ETH), Rate(251, CurrencyPair(ETH, USD))))
      )

      Then("we send an order to buy 1.27.BTC")
      exchangeConnector.executeOnExchange(Order(Quantity(1.27, BTC), Rate(7900, CurrencyPair(BTC, USD)), "ourId1", Side.Bid)) was called

      When("the market fills the first order")
      oms onMessage MarketReport("exchangeId1", "ourId1", Quantity(1.27, BTC), Rate(7900, CurrencyPair(BTC, USD)))

      Then("we send an order to buy 42.33.ETH")
      exchangeConnector.executeOnExchange(Order(Quantity(42.33, ETH), Rate(.03, CurrencyPair(ETH, BTC)), "ourId2", Side.Bid)) was called

      When("the market fills the second order")
      oms onMessage MarketReport("exchangeId2", "ourId2", Quantity(42.33, ETH), Rate(.03, CurrencyPair(ETH, BTC)))

      Then("we send an order to sell 42.33.ETH")
      exchangeConnector.executeOnExchange(Order(Quantity(42.33, ETH), Rate(250, CurrencyPair(ETH, USD)), "ourId3", Side.Ask)) was called

      When("the market fills the third order")
      oms onMessage MarketReport("exchangeId3", "ourId3", Quantity(42.33, ETH), Rate(250, CurrencyPair(ETH, USD)))

      Then("the balance is now bigger")
      oms.funds(USD) shouldBe Quantity(10582.5, USD)
    }
  }
}
