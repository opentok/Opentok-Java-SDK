/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.util;

import com.opentok.exception.OpenTokException;
import com.vonage.jwt.Jwt;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class TokenGenerator {

    public static final String ISSUER = "iss";
    public static final String ISSUER_TYPE = "ist";
    public static final String ISSUED_AT = "iat";
    public static final String EXP = "exp";
    public static final String PROJECT_ISSUER_TYPE = "project";


    // Used by the REST Endpoints
    public static String generateToken(final Integer apiKey, final String apiSecret)
            throws OpenTokException {

        final JwtClaims claims = new JwtClaims();
        //This is the default expire time we use for rest endpoints.
        final long defaultExpireTime = Instant.now().plus(3, ChronoUnit.MINUTES).getEpochSecond();
        return generateToken(claims, defaultExpireTime, apiKey, apiSecret);
    }

    public static String generateToken(Map<String, Object> claims, final long expireTime,
                                       final String applicationId, final Path privateKeyPath) throws OpenTokException {
        try {
            claims.put(EXP, expireTime);
            return Jwt.builder()
                    .applicationId(applicationId)
                    .privateKeyPath(privateKeyPath)
                    .claims(claims)
                    .build().generate();
        }
        catch (IOException ex) {
            throw new OpenTokException("Could not generate token: " + ex.getMessage());
        }
    }

    public static String generateToken(final JwtClaims claims, final long expireTime,
                                final int apiKey, final String apiSecret) throws OpenTokException {
        final SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(),
                AlgorithmIdentifiers.HMAC_SHA256);

        claims.setStringClaim(ISSUER_TYPE, PROJECT_ISSUER_TYPE);
        claims.setIssuer(apiKey + "");
        claims.setGeneratedJwtId(); // JTI a unique identifier for the JWT.
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
