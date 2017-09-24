package com.rlam.arbitrader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.math.BigDecimal;

public enum Market {

	BTCUSD("BTC-USD", 1, 0, new BigDecimal(".01")),
	ETHUSD("ETH-USD", 2, 0, new BigDecimal(".01")),
	ETHBTC("ETH-BTC", 2, 1, new BigDecimal(".00001")),
	LTCUSD("LTC-USD", 3, 0, new BigDecimal(".01")),
	LTCBTC("LTC-BTC", 3, 1, new BigDecimal(".00001"));
	
	private final int row;
	private final int column;
	private final String market;
	private final BigDecimal increment;
	private final String url = "https://api.gdax.com/products/";

	private BigDecimal price;
	
	Market(String market, int row, int column, BigDecimal increment) {
		this.market = market;
		this.row = row;
		this.column = column;
		this.increment = increment;
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

	public BigDecimal getIncrement() { return increment; }
	
	public BigDecimal getPrice() {
		return price;
	}

	public BigDecimal getInversePrice() { return BigDecimal.ONE.divide(price,7, BigDecimal.ROUND_HALF_UP); }

	public BigDecimal getPrice(String from, String to) {
		String market = from + "-" + to;
		String marketInverse = to + "-" + from;
		if (market.equals(this.market)) {
			return getPrice();
		} else if (marketInverse.equals(this.market)) {
			return getInversePrice();
		} else {
			return BigDecimal.ZERO;
		}
	}

	public void updatePrice(BigDecimal price) {
		this.price = price;
	}
	
	public void fetchPriceFromServer() {
		HttpResponse<JsonNode> response = null;
		
		try {
			response = Unirest.get(url + market + "/ticker").asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(response.getBody().toString());
        this.price = jsonObject.get("price").getAsBigDecimal();
	}
	
}
