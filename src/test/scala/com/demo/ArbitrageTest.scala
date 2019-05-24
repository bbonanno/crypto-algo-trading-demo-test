package com.demo

import com.demo.Currency._
import com.demo.exchange._
import org.mockito.scalatest.IdiomaticMockito
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class ArbitrageTest extends FeatureSpec with GivenWhenThen with IdiomaticMockito with Matchers {

  val exchangeConnector = mock[ExchangeConnector]
  val clackatron        = new Clackatron(exchangeConnector)

  feature("When an arbitrage opportunity occurs the system has to execute the required orders to make money") {

    scenario("Triangle man is happy") {
      Given("we have some money")
      clackatron onMessage AvailableFunds(Seq(Quantity(10000, USD)))

      When("the market data shows an arbitrage opportunity")
      clackatron onMessage MarketData(
        CurrencyPair(BTC, USD),
        Seq(PriceLevel(Quantity(10, BTC), Price(7899, CurrencyPair(BTC, USD)))),
        Seq(PriceLevel(Quantity(10, BTC), Price(7900, CurrencyPair(BTC, USD))))
      )
      clackatron onMessage MarketData(
        CurrencyPair(ETH, BTC),
        Seq(PriceLevel(Quantity(100, ETH), Price(.0299, CurrencyPair(ETH, BTC)))),
        Seq(PriceLevel(Quantity(100, ETH), Price(.03, CurrencyPair(ETH, BTC))))
      )
      clackatron onMessage MarketData(
        CurrencyPair(ETH, USD),
        Seq(PriceLevel(Quantity(50, ETH), Price(250, CurrencyPair(ETH, USD)))),
        Seq(PriceLevel(Quantity(50, ETH), Price(251, CurrencyPair(ETH, USD))))
      )

      Then("we send an order to buy 1.27 BTC at 7900 USD")
      exchangeConnector.executeOnExchange(Order(Quantity(1.27, BTC), Price(7900, CurrencyPair(BTC, USD)), ClackatronOrderId("cid1"), Side.Bid)) was called

      When("the market fills the first order")
      clackatron onMessage MarketReport(ClackatronOrderId("cid1"), ExchangeOrderId("eid1"), Quantity(1.27, BTC), Price(7900, CurrencyPair(BTC, USD)))

      Then("we send an order to buy 42.33 ETH at .03 ETH")
      exchangeConnector.executeOnExchange(Order(Quantity(42.33, ETH), Price(.03, CurrencyPair(ETH, BTC)), ClackatronOrderId("cid2"), Side.Bid)) was called

      When("the market fills the second order")
      clackatron onMessage MarketReport(ClackatronOrderId("cid2"), ExchangeOrderId("eid2"), Quantity(42.33, ETH), Price(.03, CurrencyPair(ETH, BTC)))

      Then("we send an order to sell 42.33 ETH at 250 USD")
      exchangeConnector.executeOnExchange(Order(Quantity(42.33, ETH), Price(250, CurrencyPair(ETH, USD)), ClackatronOrderId("cid3"), Side.Ask)) was called

      When("the market fills the third order")
      clackatron onMessage MarketReport(ClackatronOrderId("cid3"), ExchangeOrderId("eid3"), Quantity(42.33, ETH), Price(250, CurrencyPair(ETH, USD)))

      Then("the balance is now bigger")
      clackatron.funds(USD) shouldBe Quantity(10582.5, USD)
    }
  }
}
