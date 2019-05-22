package com.demo

import com.demo.exchange._
import org.mockito.scalatest.MockitoSugar
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class ArbitrageTest extends FeatureSpec with GivenWhenThen with MockitoSugar with Matchers {

  val exchangeConnector = mock[ExchangeConnector]
  val oms               = new OMS(exchangeConnector)

  feature("When an arbitrage opportunity occurs the system has to execute the required orders to make money") {

    scenario("Triangular arbitrage") {
      Given("we have some money")
      oms onMessage AvailableFunds(Seq(Quantity(10000, Currency("USD"))))

      And("the following market data")
      oms onMessage MarketData(
        CurrencyPair(Currency("BTC"), Currency("USD")),
        Seq(PriceLevel(Quantity(10, Currency("BTC")), Rate(7899, CurrencyPair(Currency("BTC"), Currency("USD"))))),
        Seq(PriceLevel(Quantity(10, Currency("BTC")), Rate(7900, CurrencyPair(Currency("BTC"), Currency("USD")))))
      )
      oms onMessage MarketData(
        CurrencyPair(Currency("ETH"), Currency("BTC")),
        Seq(PriceLevel(Quantity(100, Currency("ETH")), Rate(.0299, CurrencyPair(Currency("ETH"), Currency("BTC"))))),
        Seq(PriceLevel(Quantity(100, Currency("ETH")), Rate(.03, CurrencyPair(Currency("ETH"), Currency("BTC")))))
      )
      oms onMessage MarketData(
        CurrencyPair(Currency("ETH"), Currency("USD")),
        Seq(PriceLevel(Quantity(50, Currency("ETH")), Rate(250, CurrencyPair(Currency("ETH"), Currency("USD"))))),
        Seq(PriceLevel(Quantity(50, Currency("ETH")), Rate(251, CurrencyPair(Currency("ETH"), Currency("USD")))))
      )

      Then("we send an order to buy 1.27.BTC")
      verify(exchangeConnector).executeOnExchange(
        Order(
          Quantity(1.27, Currency("BTC")),
          Rate(7900, CurrencyPair(Currency("BTC"), Currency("USD"))),
          "ourId1",
          Side.Bid
        ))

      When("the market fills the first order")
      oms onMessage MarketReport("exchangeId1", "ourId1", Quantity(1.27, Currency("BTC")), Rate(7900, CurrencyPair(Currency("BTC"), Currency("USD"))))

      Then("we send an order to buy 42.33.ETH")
      verify(exchangeConnector).executeOnExchange(
        Order(Quantity(42.33, Currency("ETH")), Rate(.03, CurrencyPair(Currency("ETH"), Currency("BTC"))), "ourId2", Side.Bid))

      When("the market fills the second order")
      oms onMessage MarketReport("exchangeId2", "ourId2", Quantity(42.33, Currency("ETH")), Rate(.03, CurrencyPair(Currency("ETH"), Currency("BTC"))))

      Then("we send an order to sell 42.33.ETH")
      verify(exchangeConnector).executeOnExchange(
        Order(Quantity(42.33, Currency("ETH")), Rate(250, CurrencyPair(Currency("ETH"), Currency("USD"))), "ourId3", Side.Ask))

      When("the market fills the third order")
      oms onMessage MarketReport("exchangeId3", "ourId3", Quantity(42.33, Currency("ETH")), Rate(250, CurrencyPair(Currency("ETH"), Currency("USD"))))

      Then("the balance is now bigger")
      oms.funds(Currency("USD")) shouldBe Quantity(10582.5, Currency("USD"))
    }
  }
}
