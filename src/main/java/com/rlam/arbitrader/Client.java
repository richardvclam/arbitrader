package com.rlam.arbitrader;

import java.io.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.*;
import com.neovisionaries.ws.client.*;

public class Client {

	public static Market BTCUSD = Market.BTCUSD;
	public static Market ETHUSD = Market.ETHUSD;
	public static Market ETHBTC = Market.ETHBTC;
	public static Market LTCUSD = Market.LTCUSD;
	public static Market LTCBTC = Market.LTCBTC;
	
	public static double[][] marketRates = {{1, 0, 0, 0},
										   {0, 1, 0, 0},
										   {0, 0, 1, 0},
										   {0, 0, 0, 1}};
	public static String[] currencies = {"USD", "BTC", "ETH", "LTC"};
	
	/**
     * The GDAX server
     */
    private static final String SERVER = "wss://ws-feed.gdax.com";

    /**
     * The timeout value in milliseconds for socket connection.
     */
    private static final int TIMEOUT = 5000;
    private static double intialStake = 1000;
    private static double total = 0;


    /**
     * The entry point of this command line application.
     */
    public static void main(String[] args) throws Exception {
    		initializeMarketRates();
	    	//String subscribe = "{\"type\": \"subscribe\",\"product_ids\": [\"BTC-USD\",\"ETH-USD\",\"ETH-BTC\",\"LTC-USD\",\"LTC-BTC\"],\"channels\": [\"ticker\"]}";
	    	Gson gson = new Gson();
	    	String subscribe = gson.toJson(new Subscribe());
        // Connect to the GDAX server.
        WebSocket ws = connect();
        // Send initial subscribe message to receive feed messages
        ws.sendText(subscribe);
    }
    
    private static void initializeMarketRates() {
    		// Fetch only the markets with the lowest volume to initially populate the market rates matrix.
    		// Limit to 3 due to the REST API's request per second limitation.
    		ETHBTC.fetchPriceFromServer();
    		updateMarketRates(ETHBTC);
    		
    		LTCUSD.fetchPriceFromServer();
    		updateMarketRates(LTCUSD);
    		
    		LTCBTC.fetchPriceFromServer();
    		updateMarketRates(LTCBTC);
    }


    /**
     * Connect to the server.
     */
    private static WebSocket connect() throws IOException, WebSocketException {
        return new WebSocketFactory()
            .setConnectionTimeout(TIMEOUT)
            .createSocket(SERVER)
            .addListener(new WebSocketAdapter() {
                // A text message arrived from the server.
                public void onTextMessage(WebSocket websocket, String message) {
                    JsonObject jsonObject = (JsonObject) new JsonParser().parse(message);
                    String market = jsonObject.get("product_id").getAsString();
                    Double price = jsonObject.get("price").getAsDouble();
                    System.out.println(market + " " + price);
                    switch (market) {
	                    case "BTC-USD":
	                    	BTCUSD.updatePrice(price);
	                    	updateMarketRates(BTCUSD);
	                    	break;
	                    case "ETH-USD":
	                    	ETHUSD.updatePrice(price);
	                    	updateMarketRates(ETHUSD);
	                    	break;
	                    case "ETH-BTC":
	                    	ETHBTC.updatePrice(price);
	                    	updateMarketRates(ETHBTC);
	                    	break;
	                    case "LTC-USD":
	                    	LTCUSD.updatePrice(price);
	                    	updateMarketRates(LTCUSD);
	                    	break;
	                    case "LTC-BTC":
	                    	LTCBTC.updatePrice(price);
	                    	updateMarketRates(LTCBTC);
	                    	break;
                    }
                    
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
                    
                    if (spt.hasNegativeCycle()) {
                        double stake = intialStake + total;
                        double beginningStake = stake;
                        for (DirectedEdge e : spt.negativeCycle()) {
                        	   Market marketObj = getMarket(currencies[e.from()], currencies[e.to()]);
                        	   System.out.printf("%10.5f %s ", stake, currencies[e.from()]);
                        	   stake *= Math.exp(-e.weight());
                        	   System.out.printf("= %10.5f %s @ %10.5f %s \n", stake, currencies[e.to()], marketObj.getPrice(), marketObj.getMarket());
                        }
                        System.out.println("Profit: " + (stake - beginningStake));
                        total += (stake - beginningStake);
                        System.out.println("Total: " + total);
                    } else {
                        System.out.println("No arbitrage opportunity");
                    }
                }
            })
            .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
            .connect();
    }


    /**
     * Wrap the standard input with BufferedReader.
     */
    private static BufferedReader getInput() throws IOException {
        return new BufferedReader(new InputStreamReader(System.in));
    }
    
    private static void updateMarketRates(Market market) {
	    	marketRates[market.getRow()][market.getColumn()] = market.getPrice();
	    	marketRates[market.getColumn()][market.getRow()] = 1 / market.getPrice();
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
	
}
