package com.rlam.arbitrader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.math.BigDecimal;

public enum Market {

	BTCUSD("BTC-USD", 1, 0, .01, MarketType.CRYPTOFIAT),
	ETHUSD("ETH-USD", 2, 0, .01 , MarketType.CRYPTOFIAT),
	ETHBTC("ETH-BTC", 2, 1, .00001, MarketType.CRYPTOCRYPTO),
	LTCUSD("LTC-USD", 3, 0, .01, MarketType.CRYPTOFIAT),
	LTCBTC("LTC-BTC", 3, 1, .00001, MarketType.CRYPTOCRYPTO);

	private enum MarketType {
		CRYPTOFIAT,
		CRYPTOCRYPTO
	}
	
	private final int row;
	private final int column;
	private final String market;
	private final double increment;
	private final MarketType marketType;
	private final String url = "https://api.gdax.com/products/";

	private double price;
	
	Market(String market, int row, int column, double increment, MarketType marketType) {
		this.market = market;
		this.row = row;
		this.column = column;
		this.increment = increment;
		this.marketType = marketType;
	}
	
	public String getMarket() {
		return market;
	}
	
	public int getRow() {
		return row;
	}
	
	public int getColumn() {
		return column;
	}

	public double getIncrement() { return increment; }
	
	public double getPrice() {
		return price;
	}

	public double getInversePrice() { return 1 / price; }

	public double getPrice(String from, String to) {
		String market = from + "-" + to;
		String marketInverse = to + "-" + from;
		if (market.equals(this.market)) {
			return getPrice();
		} else if (marketInverse.equals(this.market)) {
			return getInversePrice();
		} else {
			return 0;
		}
	}

	/**
	 * Update the price of this market. Returns TRUE if the price has changed and FALSE otherwise.
	 * @param price the current price of the market
	 * @return TRUE if market price has changed, FALSE if market price has not changed
	 */
	public void updatePrice(double price) {
		this.price = price;
	}

	/**
	 * Subtracts a value from the current market price and rounds the difference to the correct
	 * decimal place using its incremental value.
	 * @param value the value to minus
	 * @return the difference rounded to the market's incremental value
	 */
	public double subtractPrice(double value) {
		double decimals = Math.pow(10, new BigDecimal("" + value).toPlainString().split("\\.")[1].length());
		return (double) Math.round((this.price - value) * decimals) / decimals;
	}
	
	public void fetchPriceFromServer() {
		try {
			HttpResponse<JsonNode> response = Unirest.get(url + market + "/ticker").asJson();
			JsonObject jsonObject = (JsonObject) new JsonParser().parse(response.getBody().toString());
			this.price = jsonObject.get("price").getAsDouble();
		} catch (UnirestException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println("Unable to fetch " + market + " price from server. Server could possibly be down.");
			e.printStackTrace();
		}

	}
	
}
