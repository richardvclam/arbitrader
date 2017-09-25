package com.rlam.arbitrader;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.*;
import com.rlam.arbitrader.util.BellmanFordSP;
import com.rlam.arbitrader.util.DirectedEdge;
import com.rlam.arbitrader.util.EdgeWeightedDigraph;

// TODO make singleton
public class MarketListener implements Runnable {

	public static String[] currencies = {"USD", "BTC", "ETH", "LTC"};

	public HashMap<String, Market> markets;
	
//	public BigDecimal[][] marketRates = {{BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO},
//								         {BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO},
//								         {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO},
//								         {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE}};
	public double[][] marketRates = {{1, 0, 0, 0},
									 {0, 1, 0, 0},
									 {0, 0, 1, 0},
									 {0, 0, 0, 1}};
	private Thread thread;
	private App app;
	
	/**
     * The GDAX server
     */
    private static final String SERVER = "wss://ws-feed.gdax.com";

    /**
     * The timeout value in milliseconds for socket connection.
     */
    private static final int TIMEOUT = 5000;
    private static double initialStake = 1000;
    private static double total = 0;

    public MarketListener(App app) {
    	this.app = app;
    	markets = new HashMap<>();
    	for (Market m : Market.values()) {
    		markets.put(m.getMarket(), m);
	    }
    }
    
    private void initializeMarketRates() {
        // Fetch only the markets with the lowest volume to initially populate the market rates matrix.
        // Limit to 3 due to the REST API's request per second limitation.
	    String[] marketsToFetch = {"ETH-BTC", "LTC-USD"};

	    for (String marketID : marketsToFetch) {
		    Market market = markets.get(marketID);
		    market.fetchPriceFromServer();
		    updateMarketRates(market);
	    }
    }

    /**
     * Connect to the server.
     */
    private WebSocket connect() throws IOException, WebSocketException {
        return new WebSocketFactory()
            .setConnectionTimeout(TIMEOUT)
            .createSocket(SERVER)
            .addListener(new WebSocketAdapter() {
                // A text message arrived from the server.
                public void onTextMessage(WebSocket websocket, String message) {
                    JsonObject jsonObject = (JsonObject) new JsonParser().parse(message);
                    String marketID = jsonObject.get("product_id").getAsString();
                    double price = jsonObject.get("price").getAsDouble();
	                System.out.println(marketID + " " + price);

	                Market market = markets.get(marketID);
	                market.updatePrice(price);
	                updateMarketRates(market);

                    ArrayList<String[]> opportunity = arbitrage();
                    if (opportunity.size() > 2) {
                    	app.onArbitrageOpportunity(opportunity);
                    }
                    app.onPriceChange(market);
                }
            })
            .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
            .connect();
    }
    
    private void updateMarketRates(Market market) {
	    	marketRates[market.getRow()][market.getColumn()] = market.getPrice();
	    	marketRates[market.getColumn()][market.getRow()] = market.getInversePrice();
    }
    
    private Market getMarket(String from, String to) {
    		for (Market m : Market.values()) {
    			String market = m.getMarket();
    			
    			if (market.equals(from + "-" + to) || market.equals(to + "-" + from)) {
    				return m;
    			}
    		}
    		return null;
    }

    public ArrayList<String[]> arbitrage() {
	    // V currencies
	    int V = currencies.length;

	    // create complete network
	    EdgeWeightedDigraph G = new EdgeWeightedDigraph(V);
	    for (int v = 0; v < V; v++) {
		    for (int w = 0; w < V; w++) {
			    double rate = marketRates[v][w];
			    DirectedEdge e = new DirectedEdge(v, w, -Math.log(rate));
			    G.addEdge(e);
		    }
	    }

	    // find negative cycle
	    BellmanFordSP spt = new BellmanFordSP(G, 0);

	    ArrayList<String[]> transactions = new ArrayList<>();
	    if (spt.hasNegativeCycle()) {
		    double stake = app.usdBalance + total;
		    double beginningStake = stake;
		    System.out.println("=========================================");
		    for (DirectedEdge e : spt.negativeCycle()) {
		        String[] transaction = { currencies[e.from()],  currencies[e.to()] };
			    Market marketObj = getMarket(currencies[e.from()], currencies[e.to()]);
			    System.out.printf("%10.5f %s ", stake, currencies[e.from()]);
			    stake *= Math.exp(-e.weight());
			    System.out.printf("= %10.5f %s @ %10.5f %s \n", stake, currencies[e.to()], marketObj.getPrice(), marketObj.getMarket());
//			    count++;
			    transactions.add(transaction);
		    }
		    System.out.println("=========================================");
//		    System.out.println("Profit: " + (stake - beginningStake));
//		    total += (stake - beginningStake);
//		    System.out.println("Total: " + total);

		    // This is a precaution as the algorithm sometimes thinks there is an opportunity when there is only 2
		    // currencies to trade. We need atleast 3 currencies for there to be a valid opportunity.
//		    return count > 2;


	    } else {
		    System.out.println("No arbitrage opportunity");
	    }
//	    for (int i = 0; i < marketRates.length; i++) {
//	    	for (int j = 0; j < marketRates[0].length; j++) {
//	    		System.out.print(marketRates[i][j].toString() + " ");
//		    }
//		    System.out.println();
//	    }
	    return transactions;
    }

	@Override
	public void run() {
		initializeMarketRates();
		Gson gson = new Gson();
		String subscribe = gson.toJson(new Subscribe());
		try {
			// Connect to the GDAX server.
			WebSocket ws = connect();
			// Send initial subscribe message to receive feed messages
			ws.sendText(subscribe);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() {
    	if (thread == null) {
    		thread = new Thread(this);
    		thread.start();
	    }
	}
}
