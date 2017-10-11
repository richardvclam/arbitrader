package com.rlam.arbitrader;

import org.knowm.xchange.gdax.dto.GDAXException;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public interface WSAuth {

	@GET
	@Path("users/self/verify")
	public void getWSAuth(@HeaderParam("CB-ACCESS-KEY") String apiKey, @HeaderParam("CB-ACCESS-SIGN") ParamsDigest signer,
	                      @HeaderParam("CB-ACCESS-TIMESTAMP") SynchronizedValueFactory<Long> nonce, @HeaderParam("CB-ACCESS-PASSPHRASE") String passphrase) throws GDAXException, IOException;
}
