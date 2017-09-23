package com.rlam.arbitrader;

import java.util.ArrayList;

public class App {

	public static final boolean test = true;

	public static String[] currencies = {"USD", "BTC", "ETH", "LTC"};
	public static double[][] marketRates = {{1, 0, 0, 0},
											{0, 1, 0, 0},
											{0, 0, 1, 0},
											{0, 0, 0, 1}};
	private static double initialStake = 1000;
	private ArrayList<String[]> opportunity;
	private double orderPrice;
	private String orderID;
	private boolean inTransaction;

	public App() {
		inTransaction = false;
		orderPrice = 0;
		orderID = "";
	}

	public void onArbitrageOpportunity(ArrayList<String[]> opportunity) {
		if (!inTransaction) {
			System.out.println("There is an opportunity!");
			this.opportunity = opportunity;
			// Set a limit buy order for first transaction
			Market marketObj = getMarket(opportunity.get(0)[0], opportunity.get(0)[1]);
			orderPrice = marketObj.getPrice() - marketObj.getIncrement();
			System.out.println("Buying @ " + orderPrice);
			inTransaction = true;
			if (!test) {
				// TODO write the real HTTP POST requests
			}
		}
	}

	public void onPriceChange(Market marketObj) {

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
    	MarketListener listener = new MarketListener(app);
    	listener.start();

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
