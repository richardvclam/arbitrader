package com.rlam.arbitrader;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class GDAXUtil {

	public static String signMessage(String secret, String requestPath, String method, String body, String timestamp) {
		try {
			byte[] decodedSecretKey = Base64.getDecoder().decode(secret);

			String preHash = timestamp + method + requestPath + body;

			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKey = new SecretKeySpec(decodedSecretKey, "HmacSHA256");
			mac.init(secretKey);

			return Base64.getEncoder().encode(mac.doFinal(preHash.getBytes())).toString();
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {

		}
		return "";
	}

}
