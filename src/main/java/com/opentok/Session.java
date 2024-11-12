/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.opentok.exception.OpenTokException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import com.opentok.exception.InvalidArgumentException;
import com.opentok.util.Crypto;
import com.opentok.util.TokenGenerator;
import org.apache.commons.codec.binary.Base64;
import org.jose4j.jwt.JwtClaims;


/**
* Represents an OpenTok session. Use the {@link OpenTok#createSession(SessionProperties properties)}
* method to create an OpenTok session. Use the {@link #getSessionId()} method of the Session object
* to get the session ID.
*/
public class Session {
    private String sessionId, apiSecret, applicationId;
    private Path privateKeyPath;
    private int apiKey;
    private SessionProperties properties;
    
    protected Session(String sessionId, int apiKey, String apiSecret) {
        this(sessionId, apiKey, apiSecret, new SessionProperties.Builder().build());
    }
    
    protected Session(String sessionId, int apiKey, String apiSecret, SessionProperties properties) {
        this.sessionId = sessionId;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.properties = properties;
    }

    protected Session(String sessionId, String applicationId, Path privateKeyPath) {
        this(sessionId, applicationId, privateKeyPath, new SessionProperties.Builder().build());
    }

    protected Session(String sessionId, String applicationId, Path privateKeyPath, SessionProperties properties) {
        this.sessionId = sessionId;
        this.properties = properties;
        this.applicationId = applicationId;
        this.privateKeyPath = privateKeyPath;
    }
    
    /**
    * Returns the OpenTok API key used to generate the session.
    */
    public int getApiKey() {
        return apiKey;
    }

    /**
    * Returns the session ID, which uniquely identifies the session.
    */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
    * Returns the properties defining the session. These properties include:
    *
    * <ul>
    *     <li>The location hint IP address.</li>
    *     <li>Whether the session's streams will be transmitted directly between peers
    *     or using the OpenTok media server.</li>
    * </ul>
    */
    public SessionProperties getProperties() {
        return properties;
    }
    
    /**
     * Generates the token for the session. The role is set to publisher, the token expires in
     * 24 hours, and there is no connection data.
     *
     * @return The token string.
     *
     * @see #generateToken(TokenOptions tokenOptions)
     */
    public String generateToken() throws OpenTokException {
        return this.generateToken(new TokenOptions.Builder().build());
    }

    /**
     * Creates a token for connecting to an OpenTok session. In order to authenticate a user
     * connecting to an OpenTok session that user must pass an authentication token along with
     * the API key.
     *
     * @param tokenOptions This TokenOptions object defines options for the token.
     * These include the following:
     *
     * <ul>
     *    <li>The role of the token (subscriber, publisher, or moderator)</li>
     *    <li>The expiration time of the token</li>
     *    <li>Connection data describing the end-user</li>
     * </ul>
     *
     * @return The token string.
     */
    public String generateToken(TokenOptions tokenOptions) throws OpenTokException {

        if (tokenOptions == null) {
            throw new InvalidArgumentException("Token options cannot be null");
        }

        Role role = tokenOptions.getRole();
        String data = tokenOptions.getData();
        int nonce = new Random().nextInt();
        long iat = System.currentTimeMillis() / 1000;
        long exp = tokenOptions.getExpireTime();

        if (exp == 0) {
            exp = iat + (60 * 60 * 24); // 1 day
        } else if (exp < iat) {
            throw new InvalidArgumentException(
                    "Expire time must be in the future. Relative time: " + (exp - iat)
            );
        } else if (exp > (iat + (60 * 60 * 24 * 30) /* 30 days */)) {
            throw new InvalidArgumentException(
                    "Expire time must be in the next 30 days. Too large by " + (exp - (iat + (60 * 60 * 24 * 30)))
            );
        }

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("session_id", sessionId);
        claims.put("nonce", nonce);
        claims.put("role", role.toString());
        claims.put("scope", "session.connect");
        if (tokenOptions.getInitialLayoutClassList() != null) {
            claims.put("initial_layout_class_list", String.join(" ", tokenOptions.getInitialLayoutClassList()));
        }
        if (data != null) {
            if (data.length() > 1000) {
                throw new InvalidArgumentException(
                        "Connection data must be less than 1000 characters. length: " + data.length()
                );
            }
            try {
                claims.put("connection_data", tokenOptions.isLegacyT1Token() ? URLEncoder.encode(data, "UTF-8") : data);
            }
            catch (UnsupportedEncodingException e) {
                throw new InvalidArgumentException(
                        "Error during URL encode of your connection data: " + e.getMessage()
                );
            }
        }

        if (tokenOptions.isLegacyT1Token()) {
            // Token format
            //
            // | ------------------------------  tokenStringBuilder ----------------------------- |
            // | "T1=="+Base64Encode(| --------------------- innerBuilder --------------------- |)|
            //                       | "partner_id={apiKey}&sig={sig}:| -- dataStringBuilder -- |

            StringBuilder dataStringBuilder = new StringBuilder()
                    .append("create_time=").append(iat)
                    .append("&expire_time=").append(exp);

            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                dataStringBuilder.append('&').append(entry.getKey()).append('=').append(entry.getValue());
            }


            StringBuilder tokenStringBuilder = new StringBuilder();
            try {
                tokenStringBuilder.append("T1==");

                String innerBuilder = "partner_id=" +
                        this.apiKey +
                        "&sig=" +
                        Crypto.signData(dataStringBuilder.toString(), this.apiSecret) +
                        ":" +
                        dataStringBuilder;

                tokenStringBuilder.append(
                        Base64.encodeBase64String(innerBuilder.getBytes(StandardCharsets.UTF_8))
                                .replace("+", "-")
                                .replace("/", "_")
                );

            }
            catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new OpenTokException("Could not generate token, a signing error occurred.", e);
            }
            return tokenStringBuilder.toString();
        }
        else if (applicationId == null && privateKeyPath == null && apiKey != 0 && apiSecret != null) {
            JwtClaims jwtClaims = new JwtClaims();
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                jwtClaims.setClaim(entry.getKey(), entry.getValue());
            }
            return TokenGenerator.generateToken(jwtClaims, exp, apiKey, apiSecret);
        }
        else if (applicationId != null && privateKeyPath != null) {
            return TokenGenerator.generateToken(claims, exp, applicationId, privateKeyPath);
        }
        else {
            throw new IllegalStateException("Insufficient auth credentials.");
        }
    }
}
