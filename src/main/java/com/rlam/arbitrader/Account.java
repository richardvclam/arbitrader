package com.rlam.arbitrader;


import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.gdax.GDAXExchange;
import org.knowm.xchange.gdax.service.GDAXDigest;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.utils.jackson.CurrencyPairDeserializer;
import si.mazi.rescu.RestInvocation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;

public class Account {

	private Exchange gdax;

	public Account(String apiKey, String secretKey, String passphrase) {
		ExchangeSpecification exSpec = new GDAXExchange().getDefaultExchangeSpecification();
		exSpec.setExchangeSpecificParametersItem("passphrase", passphrase);
		exSpec.setApiKey(apiKey);
		exSpec.setSecretKey(secretKey);

		this.gdax = ExchangeFactory.INSTANCE.createExchange(exSpec);
	}

	public double getAvailableBalance(Currency currency) {
		try {
			return gdax.getAccountService().getAccountInfo().getWallet().getBalance(currency).getAvailable().doubleValue();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public String placeOrder(OrderType orderType, String product, double size, double limitPrice, String clientOrderID) {
		LimitOrder limitOrder = new LimitOrder(
				orderType,
				new BigDecimal("" + size),
				CurrencyPairDeserializer.getCurrencyPairFromString(product),
				clientOrderID,
				Date.from(Instant.now()),
				new BigDecimal("" + limitPrice)
		);
		try {
			return gdax.getTradeService().placeLimitOrder(limitOrder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void cancelOrder(String orderID) {
		try {
			gdax.getTradeService().cancelOrder(orderID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {


//		System.out.println(gdax.getAccountService().getAccountInfo().getWallet().getBalance(Currency.USD).getAvailable());
//		System.out.println(gdax.getTradeService().getOpenOrders().toString());
//		TradeService tradeService = gdax.getTradeService();
//		tradeService.createTradeHistoryParams();
//		System.out.println(tradeService.getTradeHistory(tradeService.createTradeHistoryParams()).getTrades().toString());

//		GDAXDigest digest = GDAXDigest.createInstance(exSpec.getSecretKey());
//		RestInvocation restInvocation = new RestInvocation();
//		GDAXAccountService accountService = new GDAXAccountService(gdax);
//		System.out.println(accountService.getCoinbaseExAccountInfo().toString());
	}

}
