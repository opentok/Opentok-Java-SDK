/**
 * OpenTok Java SDK
 * Copyright (C) 2015 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class Crypto {
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    // -- credit: https://gist.github.com/ishikawa/88599

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static String signData(String data, String key)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }

    public static List<String> decodeSessionId(String sessionId) throws UnsupportedEncodingException {
        sessionId = sessionId.substring(2);
        sessionId = sessionId.replaceAll("-", "+").replaceAll("_", "/");
        byte[] buffer = Base64.decodeBase64(sessionId);
        sessionId = new String(buffer, "UTF-8");
        return new ArrayList<String>(Arrays.asList(sessionId.split("~")));
    }
}