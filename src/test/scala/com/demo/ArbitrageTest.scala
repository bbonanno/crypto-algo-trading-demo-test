package com.demo

import com.demo.Currency._
import com.demo.exchange._
import org.mockito.scalatest.MockitoSugar
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class ArbitrageTest extends FeatureSpec with GivenWhenThen with MockitoSugar with Matchers with TestDsl {

  val exchangeConnector = mock[ExchangeConnector]
  implicit val oms      = new OMS(exchangeConnector)

  feature("When an arbitrage opportunity occurs the system has to execute the required orders to make money") {

    scenario("Triangular arbitrage") {
      Given("we have some money")
      updateAvailableFunds(Quantity(10000, USD))

      And("the following market data")
      onMarketData(BTC / USD, asks = Seq(PriceLevel(Quantity(10, BTC), Rate(7900, BTC / USD))))
      onMarketData(ETH / BTC, asks = Seq(PriceLevel(Quantity(100, ETH), Rate(.03, ETH / BTC))))
      onMarketData(ETH / USD, bids = Seq(PriceLevel(Quantity(50, ETH), Rate(250, ETH / USD))))

      Then("we send an order to buy 1.27.BTC")
      verify(exchangeConnector).executeOnExchange(Order(Quantity(1.27, BTC), Rate(7900, BTC / USD), "ourId1", Side.Bid))

      When("the market fills the first order")
      onMarketReport(Quantity(1.27, BTC), Rate(7900, BTC / USD), "exchangeId1", "ourId1")

      Then("we send an order to buy 42.33.ETH")
      verify(exchangeConnector).executeOnExchange(Order(Quantity(42.33, ETH), Rate(.03, ETH / BTC), "ourId2", Side.Bid))

      When("the market fills the second order")
      onMarketReport(Quantity(42.33, ETH), Rate(.03, ETH / BTC), "exchangeId2", "ourId2")

      Then("we send an order to sell 42.33.ETH")
      verify(exchangeConnector).executeOnExchange(Order(Quantity(42.33, ETH), Rate(250, ETH / USD), "ourId3", Side.Ask))

      When("the market fills the third order")
      onMarketReport(Quantity(42.33, ETH), Rate(250, ETH / USD), "exchangeId3", "ourId3")

      Then("the balance is now bigger")
      oms.funds(USD) shouldBe Quantity(10582.5, USD)
    }
  }
}
