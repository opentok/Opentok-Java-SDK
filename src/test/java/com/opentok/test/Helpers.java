package com.opentok.test;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;

public class Helpers {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static Map<String, String> decodeToken(String token) throws UnsupportedEncodingException {
        Map<String, String> tokenData = new HashMap<String, String>();
        token = token.substring(4);
        byte[] buffer = Base64.decodeBase64(token);
        String decoded = new String(buffer, "UTF-8");
        String[] decodedParts = decoded.split(":");
        for (String part : decodedParts) {
            tokenData.putAll(decodeFormData(part));
        }
        return tokenData;
    }

    public static boolean verifyTokenSignature(String token, String apiSecret) throws
            UnsupportedEncodingException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        token = token.substring(4);
        byte[] buffer = Base64.decodeBase64(token);
        String decoded = new String(buffer, "UTF-8");
        String[] decodedParts = decoded.split(":");
        String signature = decodeToken(token).get("sig");
        return (signature.equals(signData(decodedParts[1], apiSecret)));
    }

    private static Map<String, String> decodeFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> decodedFormData = new HashMap<String, String>();
        String[] pairs = formData.split("\\&");
        for (int i = 0; i < pairs.length; i++) {
            String[] fields = pairs[i].split("=");
            String name = URLDecoder.decode(fields[0], "UTF-8");
            String value = URLDecoder.decode(fields[1], "UTF-8");
            decodedFormData.put(name, value);
        }
        return decodedFormData;
    }

    // -- credit: https://gist.github.com/ishikawa/88599

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    private static String signData(String data, String key)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }
}
