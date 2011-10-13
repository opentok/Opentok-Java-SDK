package com.opentok.util;

import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class GenerateMac {
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	private static String hexmap[];

	static {
		//No good way to convert to hex, built a map table
		hexmap = new String[256];
		hexmap[0] = "00";
		hexmap[1] = "01";
		hexmap[2] = "02";
		hexmap[3] = "03";
		hexmap[4] = "04";
		hexmap[5] = "05";
		hexmap[6] = "06";
		hexmap[7] = "07";
		hexmap[8] = "08";
		hexmap[9] = "09";
		hexmap[10] = "0a";
		hexmap[11] = "0b";
		hexmap[12] = "0c";
		hexmap[13] = "0d";
		hexmap[14] = "0e";
		hexmap[15] = "0f";
		for (int i = 16; i < 256; i++) {
			hexmap[i] = Integer.toHexString(i);
		}
	}


	public static String get_hex(byte byt) {
		return hexmap[((short)(byt & 0xff))];
	}

	public static String calculateRFC2104HMAC(String data, String key) throws java.security.SignatureException {
	/**
	*@param data
	* The data to be signed. (The wbxml as a string of Hex digits.)
	*@param key
	* The signing key. (E.g. USERPIN of '1234')
	*/
		StringBuilder hexMAC = new StringBuilder();
		try {

			// Get an hmac_sha1 key from the raw key bytes
			byte[] keyBytes = key.getBytes();
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);

			// Get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			// Compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());

			for(int i=0;i<rawHmac.length;i++) {
				hexMAC.append(get_hex(rawHmac[i]));
			}
			// Convert raw bytes to Hex
			//byte[] hexBytes = new Hex().encode(rawHmac);

			//  Covert array of Hex bytes to a String
			//result = new String(hexBytes, "ISO-8859-1");
		}
		catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
			}
		return hexMAC.toString();
		}
}