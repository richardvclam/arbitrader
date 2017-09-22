package com.rlam.arbitrader;

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
	
}
