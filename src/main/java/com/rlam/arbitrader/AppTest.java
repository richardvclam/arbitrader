package com.rlam.arbitrader;

import java.util.ArrayList;

public class AppTest {

	public static final boolean test = true;

	public static String[] currencies = {"USD", "BTC", "ETH", "LTC"};
	public static double[][] marketRates = {{1, 0, 0, 0},
											{0, 1, 0, 0},
											{0, 0, 1, 0},
											{0, 0, 0, 1}};
	public double usdBalance = 5000;
	private ArrayList<String[]> opportunity;
	public double balance = 5000;
	private double orderPrice;
	private double orderSize;
	private String orderID;
	private boolean inTransaction;
	private boolean sellBack;
	private String currentMarket;
	private MarketListenerTest listener;

	public AppTest() {
		inTransaction = false;
		orderPrice = 0;
		orderSize = 0;
		orderID = "";
		currentMarket = "";
		sellBack = false;

		listener = new MarketListenerTest(this);
		listener.start();
	}

	public void onArbitrageOpportunity(ArrayList<String[]> opportunity) {
		if (!inTransaction) {
			System.out.println("There is an opportunity!");
			this.opportunity = opportunity;

			placeOrder();
			if (!test) {
				// TODO write the real HTTP POST requests
			}
		}
	}

	public void onPriceChange(Market marketObj) {
		if (marketObj.getMarket().equals(currentMarket)) {
			if (orderPrice >= marketObj.getPrice()) {
				fillOrder();
				if (opportunity.size() > 0 || !sellBack) {
					placeOrder();
				} else {
					// We should have completed our transaction in this case.
					inTransaction = false;
					sellBack = false;
					usdBalance = balance;
					System.out.println("Completed opportunity! Balance: " + balance);
				}

			} else if (marketObj.subtractPrice(orderPrice) > marketObj.getIncrement()) {
				System.out.println("Price diff is " + marketObj.subtractPrice(orderPrice) + " " + marketObj.getMarket());
				// Market price is increasing from our order price
				if (opportunity.size() > 0 && opportunity.size() < 3) {
					double tmpBalance = balance;
					for (int i = 0; i < opportunity.size(); i++) {
						tmpBalance = tmpBalance * getMarket(opportunity.get(i)[0], opportunity.get(i)[1]).getPrice(opportunity.get(i)[0], opportunity.get(i)[1]);
					}
					System.out.println("If we sell at current prices, we'll profit " + tmpBalance);
					if (tmpBalance > usdBalance) {
						cancelOrder();
						placeOrder();
					} else {
						// Try to sell back to USD
						tmpBalance = balance * getMarket(opportunity.get(0)[0], "USD").getPrice();
						if (tmpBalance > usdBalance) {
							cancelOrder();
							placeOrder(opportunity.get(0)[0], "USD");
							System.out.println("Trying to sell back for " + tmpBalance + " USD.");
						} else {
							System.out.println("Can't sell back to USD because we'd lose money. Calculated balance: " + tmpBalance + " USD");
						}
					}
				} else {
					cancelOrder();
				}
			}
		}
	}

	public void placeOrder() {
		// Set a limit buy order for transaction
		Market marketObj = getMarket(opportunity.get(0)[0], opportunity.get(0)[1]);
		orderPrice = marketObj.subtractPrice(marketObj.getIncrement());
		orderSize = balance * marketObj.getPrice(opportunity.get(0)[0], opportunity.get(0)[1]);
		currentMarket = marketObj.getMarket();
		System.out.println("Placed an order of " + orderSize + " " + opportunity.get(0)[1] + " @ " + orderPrice + " " +  marketObj.getMarket());
		inTransaction = true;
	}

	public void placeOrder(String from, String to) {
		Market marketObj = getMarket(from, to);
		orderPrice = marketObj.subtractPrice(marketObj.getIncrement());
		orderSize = balance * marketObj.getPrice(from, to);
		currentMarket = marketObj.getMarket();
		System.out.println("Placed an order of " + orderSize + " " + to + " @ " + orderPrice + " " +  marketObj.getMarket());
		inTransaction = true;
		sellBack = true;
	}

	public void fillOrder() {
		System.out.println("Order filled!");
		balance = orderSize;
		orderID = "";
		orderSize = 0;
		orderPrice = 0;
		currentMarket = "";
		opportunity.remove(0);
	}

	public void cancelOrder() {
//		if (opportunity.size() == 3) {
			currentMarket = "";
			inTransaction = false;
			orderPrice = 0;
			orderID = "";
			orderSize = 0;
			System.out.println("Cancelled order.");
//		} else {
			//System.out.println("Can't cancel order. We're already committed into this opportunity.");
			// TODO come up with an exit strategy
//		}

	}

	private static Market getMarket(String from, String to) {
		for (Market m : Market.values()) {
			String market = m.getMarket();

			if (market.equals(from + "-" + to) || market.equals(to + "-" + from)) {
				return m;
			}
		}
		return null;
	}

    public static void main(String[] args) {
		AppTest app = new AppTest();
    	// MarketListener runs on its own thread.
	    // MarketListener is constantly listening for price changes in the market.
	    // This application will use these prices to calculate arbitrage opportunities.


	    // After obtaining an opportunity, set a limit buy order for first transaction

	    // Obtain order_id from response

	    // On every price update, recalculate arbitrage opportunity using the price we originally
	    // entered in.

	    // TODO: If opportunity is gone, come up with a plan to mitigate risk

	    // Listen for completed orders and check if their order_id is equal to ours

	    // Repeat with next transaction
    }

//    public static void arbitrage() {
//	    // V currencies
//	    int V = currencies.length;
//
//	    // create complete network
//	    EdgeWeightedDigraph G = new EdgeWeightedDigraph(V);
//	    for (int v = 0; v < V; v++) {
//		    for (int w = 0; w < V; w++) {
//			    double rate = marketRates[v][w];
//			    DirectedEdge e = new DirectedEdge(v, w, -Math.log(rate));
//			    G.addEdge(e);
//		    }
//	    }
//
//	    // find negative cycle
//	    BellmanFordSP spt = new BellmanFordSP(G, 0);
//
//	    if (spt.hasNegativeCycle()) {
//		    double stake = intialStake + total;
//		    double beginningStake = stake;
//		    for (DirectedEdge e : spt.negativeCycle()) {
//			    Market marketObj = getMarket(currencies[e.from()], currencies[e.to()]);
//			    System.out.printf("%10.5f %s ", stake, currencies[e.from()]);
//			    stake *= Math.exp(-e.weight());
//			    System.out.printf("= %10.5f %s @ %10.5f %s \n", stake, currencies[e.to()], marketObj.getPrice(), marketObj.getMarket());
//		    }
//		    System.out.println("Profit: " + (stake - beginningStake));
//		    total += (stake - beginningStake);
//		    System.out.println("Total: " + total);
//	    } else {
//		    System.out.println("No arbitrage opportunity");
//	    }
//    }

}