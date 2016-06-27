/**
 * OpenTok Java SDK
 * Copyright (C) 2016 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.test;

import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.opentok.constants.Version;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertTrue;

public class Helpers {

    public static Map<String, Object> decodeToken(String token, Integer apiKey, String apiSecret)
            throws UnsupportedEncodingException, InvalidJwtException {
        return getClaims(token, apiKey, apiSecret);
    }

    public static boolean verifyTokenSignature(String token, Integer apiKey, String apiSecret) {

        try {
            getClaims(token, apiKey, apiSecret);
            return true;
        } catch (InvalidJwtException e) {
            return false;
        }
    }

    public static void verifyTokenAuth(int apiKey, String apiSecret, List<LoggedRequest> requests) {
        for (Request request: requests) {
            assertTrue(verifyTokenSignature(request.getHeader("X-OPENTOK-AUTH"), apiKey, apiSecret));
        }
    }

    public static void verifyUserAgent() {
        verify(RequestPatternBuilder.allRequests()
                .withHeader("User-Agent", matching(".*Opentok-Java-SDK/"
                        + Version.VERSION + ".*JRE/" + System.getProperty("java.version") + ".*")));
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
}
