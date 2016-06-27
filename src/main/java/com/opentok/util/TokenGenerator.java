package com.opentok.util;

import com.opentok.TokenOptions;
import com.opentok.exception.InvalidArgumentException;
import com.opentok.exception.OpenTokException;
import org.apache.commons.lang.StringUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class TokenGenerator {

    public static final String ISSUER = "iss";
    public static final String ISSUED_AT = "iat";
    public static final String EXP = "exp";
    public static final String SID = "sid";
    public static final String CONNECTION_DATA = "connectionData";
    public static final String ROLE = "role";
    public static final String INITIAL_LAYOUT_CLASS_LIST = "initialLayoutClassList";

    // Used by the REST Endpoints
    public static String generateToken(final Integer apiKey, final String apiSecret)
            throws OpenTokException {

        //This is the default expire time we use for rest endpoints.
        final long defaultExpireTime = System.currentTimeMillis() / 1000L + 300;  // 5 minutes
        final JwtClaims claims = new JwtClaims();
        claims.setIssuer(apiKey.toString());

        return getToken(claims, defaultExpireTime, apiSecret);
    }

    public static String generateToken(final String sessionId, final TokenOptions tokenOptions,
                                       final Integer apiKey, final String apiSecret)
            throws OpenTokException {

        List<String> sessionIdParts;
        if(sessionId == null || sessionId.isEmpty()) {
            throw new InvalidArgumentException("Session ID not valid");
        }

        try {
            sessionIdParts = Crypto.decodeSessionId(sessionId);
        } catch (UnsupportedEncodingException e) {
            throw new InvalidArgumentException("Session ID was not valid");
        }

        if (!sessionIdParts.contains(Integer.toString(apiKey))) {
            throw new InvalidArgumentException("Session ID was not valid");
        }

        if (tokenOptions == null) {
            throw new InvalidArgumentException("Token options cannot be null");
        }

        long expireTime = tokenOptions.getExpireTime();
        final long now = System.currentTimeMillis() / 1000L;
        if (expireTime == 0) {
            expireTime = now + (60 * 60 * 24); // 1 day
        } else if(expireTime < now - 1) {
            throw new InvalidArgumentException(
                    "Expire time must be in the future. relative time: " + (expireTime - now));
        } else if(expireTime > (now + (60 * 60 * 24 * 30) /* 30 days */)) {
            throw new InvalidArgumentException(
                    "Expire time must be in the next 30 days. too large by " +
                            (expireTime - (now + (60 * 60 * 24 * 30))));
        }

        final JwtClaims claims = new JwtClaims();
        claims.setIssuer(apiKey.toString());
        claims.setClaim(SID, sessionId);
        claims.setClaim(CONNECTION_DATA, tokenOptions.getData());
        claims.setClaim(ROLE, tokenOptions.getRole());
        claims.setClaim(INITIAL_LAYOUT_CLASS_LIST,
                StringUtils.join(tokenOptions.getInitialLayoutClassList(), " "));

        return getToken(claims, expireTime, apiSecret);

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
