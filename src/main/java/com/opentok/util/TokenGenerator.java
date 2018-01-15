/**
 * OpenTok Java SDK
 * Copyright (C) 2018 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.util;

import com.opentok.exception.OpenTokException;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.TimeUnit;

public class TokenGenerator {

    public static final String ISSUER = "iss";
    public static final String ISSUER_TYPE = "ist";
    public static final String ISSUED_AT = "iat";
    public static final String EXP = "exp";
    public static final String PROJECT_ISSUER_TYPE = "project";


    // Used by the REST Endpoints
    public static String generateToken(final Integer apiKey, final String apiSecret)
            throws OpenTokException {

        //This is the default expire time we use for rest endpoints.
        final long defaultExpireTime = System.currentTimeMillis() / 1000L
                + TimeUnit.MINUTES.toSeconds(3);
        final JwtClaims claims = new JwtClaims();
        claims.setIssuer(apiKey.toString());
        claims.setStringClaim(ISSUER_TYPE, PROJECT_ISSUER_TYPE);
        claims.setGeneratedJwtId(); // JTI a unique identifier for the JWT.

        return getToken(claims, defaultExpireTime, apiSecret);
    }

    private static String getToken(final JwtClaims claims, final long expireTime,
                                   final String apiSecret) throws OpenTokException {
        final SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(),
                AlgorithmIdentifiers.HMAC_SHA256);

        claims.setExpirationTime(NumericDate.fromSeconds(expireTime));
        claims.setIssuedAtToNow();

        final JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(spec);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);

        try {
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new OpenTokException(e.getMessage());
        }
    }
}
