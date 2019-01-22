/**
 * OpenTok Java SDK
 * Copyright (C) 2019 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.test;

import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.opentok.constants.Version;
import org.apache.commons.codec.binary.Base64;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.opentok.util.Crypto.signData;
import static com.opentok.util.TokenGenerator.ISSUED_AT;
import static com.opentok.util.TokenGenerator.ISSUER;
import static com.opentok.util.TokenGenerator.ISSUER_TYPE;
import static com.opentok.util.TokenGenerator.PROJECT_ISSUER_TYPE;
import static org.junit.Assert.assertTrue;

public class Helpers {

    public static final String JTI = "jti";

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

    public static boolean verifyTokenAuth(Integer apiKey, String apiSecret, List<LoggedRequest> requests) {
        for (Request request: requests) {
            if (!verifyJWTClaims(request.getHeader("X-OPENTOK-AUTH"), apiKey, apiSecret)) {
                return false;
            }
        }
        return true;
    }

    public static void verifyUserAgent() {
        verify(RequestPatternBuilder.allRequests()
                .withHeader("User-Agent", matching(".*Opentok-Java-SDK/"
                        + Version.VERSION + ".*JRE/" + System.getProperty("java.version") + ".*")));
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

    private static Map<String, Object> getClaims(final String token, final Integer apiKey,
                                                 final String apiSecret)
            throws InvalidJwtException {
        final SecretKeySpec key = new SecretKeySpec(apiSecret.getBytes(),
                AlgorithmIdentifiers.HMAC_SHA256);

        final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(30)
                .setExpectedIssuer(apiKey.toString())
                .setVerificationKey(key)
                .build();
        return jwtConsumer.processToClaims(token).getClaimsMap();
    }

    // -- credit: https://gist.github.com/ishikawa/88599

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static boolean verifyJWTClaims(String token, Integer apiKey, String apiSecret) {
        try {
            Map<String, Object> tokenData = getClaims(token, apiKey, apiSecret);
            return apiKey.toString().equals(tokenData.get(ISSUER))
                    && PROJECT_ISSUER_TYPE.equals(tokenData.get(ISSUER_TYPE))
                    && System.currentTimeMillis() / 1000 >= (long) tokenData.get(ISSUED_AT)
                    && null != tokenData.get(JTI);
        } catch (InvalidJwtException e) {
            return false;
        }
    }

}
