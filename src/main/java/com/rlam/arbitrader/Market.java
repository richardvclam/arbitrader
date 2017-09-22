package com.rlam.arbitrader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public enum Market {

	BTCUSD("BTC-USD", 1, 0),
	ETHUSD("ETH-USD", 2, 0),
	ETHBTC("ETH-BTC", 2, 1),
	LTCUSD("LTC-USD", 3, 0),
	LTCBTC("LTC-BTC", 3, 1);
	
	private final int row;
	private final int column;
	private final String market;
	private double price;
	private String url = "https://api.gdax.com/products/";
	
	private Market(String market, int row, int column) {
		this.market = market;
		this.row = row;
		this.column = column;
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
	
	public double getPrice() {
		return price;
	}
	
	public void updatePrice(double price) {
		this.price = price;
	}
	
	public double fetchPriceFromServer() {
		HttpResponse<JsonNode> response = null;
		
		try {
			response = Unirest.get(url + market + "/ticker").asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
	    	JsonObject jsonObject = (JsonObject) new JsonParser().parse(response.getBody().toString());
	    this.price = jsonObject.get("price").getAsDouble();
	    
	    	return price;
	}
	
}
