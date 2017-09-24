package com.rlam.arbitrader;

import java.math.BigDecimal;
import java.util.ArrayList;

public class App {

	public static final boolean test = true;

	public static String[] currencies = {"USD", "BTC", "ETH", "LTC"};
	public static double[][] marketRates = {{1, 0, 0, 0},
											{0, 1, 0, 0},
											{0, 0, 1, 0},
											{0, 0, 0, 1}};
	private static BigDecimal initialStake = new BigDecimal("1000");
	private ArrayList<String[]> opportunity;
	private BigDecimal balance = new BigDecimal("1000");
	private BigDecimal orderPrice;
	private BigDecimal orderSize;
	private String orderID;
	private boolean inTransaction;
	private String currentMarket;
	private MarketListener listener;

	public App() {
		inTransaction = false;
		orderPrice = BigDecimal.ZERO;
		orderSize = BigDecimal.ZERO;
		orderID = "";
		currentMarket = "";

		listener = new MarketListener(this);
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
			if (orderPrice.compareTo(marketObj.getPrice()) >= 0) {
				fillOrder();
				if (opportunity.size() > 0) {
					placeOrder();
				} else {
					// We should have completed our transaction in this case.
					inTransaction = false;
				}

			} else if (marketObj.getPrice().subtract(orderPrice).compareTo(marketObj.getIncrement()) > 0) {
				System.out.println("Price diff is " + marketObj.getPrice().subtract(orderPrice));
				// Market price is increasing from our order price
				cancelOrder();
			}
			if (opportunity.size() > 0 && opportunity.size() < 3) {
				BigDecimal tmpBalance = balance;
				for (int i = 0; i < opportunity.size(); i++) {
					tmpBalance = tmpBalance.multiply(marketObj.getPrice(opportunity.get(i)[0], opportunity.get(i)[1]));
				}
				System.out.println("If we sell at current prices, we'll profit " + tmpBalance.toString());
			}
		}
	}

	public void placeOrder() {
		if (opportunity.size() > 0) {
			// Set a limit buy order for transaction
			Market marketObj = getMarket(opportunity.get(0)[0], opportunity.get(0)[1]);
			orderPrice = marketObj.getPrice().subtract(marketObj.getIncrement());
			orderSize = balance.multiply(marketObj.getPrice(opportunity.get(0)[0], opportunity.get(0)[1]));
			currentMarket = marketObj.getMarket();
			System.out.println("Placed an order of " + orderSize.toString() + " " + opportunity.get(0)[1] + " @ " + orderPrice.toString() + " " +  marketObj.getMarket());
			inTransaction = true;
		}
	}

	public void fillOrder() {
		System.out.println("Order filled!");
		balance = orderSize;
		orderID = "";
		orderSize = BigDecimal.ZERO;
		orderPrice = BigDecimal.ZERO;
		currentMarket = "";
		opportunity.remove(0);
	}

	public void cancelOrder() {
		if (opportunity.size() == 3) {
			currentMarket = "";
			inTransaction = false;
			orderPrice = BigDecimal.ZERO;
			orderID = "";
			orderSize = BigDecimal.ZERO;
			System.out.println("Cancelled order.");
		} else {
			System.out.println("Can't cancel order. We're already committed into this opportunity.");
			// TODO come up with an exit strategy
		}

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
		App app = new App();
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
