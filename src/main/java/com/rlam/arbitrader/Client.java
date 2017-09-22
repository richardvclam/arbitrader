package com.rlam.arbitrader;

import java.io.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.*;

public class Client {

	public static double BTCUSD, ETHUSD, ETHBTC, LTCUSD, LTCBTC;
	public static int markets = 4;
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
    	//String subscribe = "{\"type\": \"subscribe\",\"product_ids\": [\"BTC-USD\",\"ETH-USD\",\"ETH-BTC\",\"LTC-USD\",\"LTC-BTC\"],\"channels\": [\"ticker\"]}";
    	Gson gson = new Gson();
    	String subscribe = gson.toJson(new Subscribe());
        // Connect to the GDAX server.
        WebSocket ws = connect();
        // Send initial subscribe message to receive feed messages
        ws.sendText(subscribe);
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
                    switch(market) {
	                    case "BTC-USD":
	                    	BTCUSD = price;
	                    	marketRates[1][0] = price;
	                    	marketRates[0][1] = 1/price;
	                    	break;
	                    case "ETH-USD":
	                    	ETHUSD = price;
	                    	marketRates[2][0] = price;
	                    	marketRates[0][2] = 1/price;
	                    	break;
	                    case "ETH-BTC":
	                    	ETHBTC = price;
	                    	marketRates[2][1] = price;
	                    	marketRates[1][2] = 1/price;
	                    	break;
	                    case "LTC-USD":
	                    	LTCUSD = price;
	                    	marketRates[3][0] = price;
	                    	marketRates[0][3] = 1/price;
	                    	break;
	                    case "LTC-BTC":
	                    	LTCBTC = price;
	                    	marketRates[3][1] = price;
	                    	marketRates[1][3] = 1/price;
	                    	break;
                    }
                    
                    // V currencies
                    int V = markets;

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
                            System.out.printf("%10.5f %s ", stake, currencies[e.from()]);
                            stake *= Math.exp(-e.weight());
                            System.out.printf("= %10.5f %s\n", stake, currencies[e.to()]);
                        }
                        System.out.println("Profit: " + (stake - beginningStake));
                        total += (stake - beginningStake);
                        System.out.println("Total: " + total);
                    }
                    else {
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
	
}
