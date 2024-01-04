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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Random;

import com.opentok.exception.InvalidArgumentException;
import com.opentok.util.Crypto;
import org.apache.commons.codec.binary.Base64;


/**
* Represents an OpenTok session. Use the {@link OpenTok#createSession(SessionProperties properties)}
* method to create an OpenTok session. Use the {@link #getSessionId()} method of the Session object
* to get the session ID.
*/
public class Session {
    private String sessionId;
    private int apiKey;
    private String apiSecret;
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
        // Token format
        //
        // | ------------------------------  tokenStringBuilder ----------------------------- |
        // | "T1=="+Base64Encode(| --------------------- innerBuilder --------------------- |)|
        //                       | "partner_id={apiKey}&sig={sig}:| -- dataStringBuilder -- |

        if (tokenOptions == null) {
            throw new InvalidArgumentException("Token options cannot be null");
        }

        Role role = tokenOptions.getRole();
        double expireTime = tokenOptions.getExpireTime(); // will be 0 if nothing was explicitly set
        String data = tokenOptions.getData();             // will be null if nothing was explicitly set
        long create_time = System.currentTimeMillis() / 1000;

        StringBuilder dataStringBuilder = new StringBuilder();
        Random random = new Random();
        int nonce = random.nextInt();
        dataStringBuilder
            .append("session_id=").append(sessionId)
            .append("&create_time=").append(create_time)
            .append("&nonce=").append(nonce)
            .append("&role=").append(role);

        if (tokenOptions.getInitialLayoutClassList() != null ) {
            dataStringBuilder.append("&initial_layout_class_list=");
            dataStringBuilder.append(String.join(" ", tokenOptions.getInitialLayoutClassList()));
        }

        long now = System.currentTimeMillis() / 1000L;
        if (expireTime == 0) {
            expireTime = now + (60*60*24); // 1 day
        }
        else if (expireTime < now) {
            throw new InvalidArgumentException(
                    "Expire time must be in the future. Relative time: "+ (expireTime - now)
            );
        }
        else if (expireTime > (now + (60*60*24*30) /* 30 days */)) {
            throw new InvalidArgumentException(
                "Expire time must be in the next 30 days. Too large by "+ (expireTime - (now + (60*60*24*30)))
            );
        }
        // NOTE: Double.toString() would print the value with scientific notation
        dataStringBuilder.append(String.format("&expire_time=%.0f", expireTime));

        if (data != null) {
            if (data.length() > 1000) {
                throw new InvalidArgumentException(
                    "Connection data must be less than 1000 characters. length: " + data.length()
                );
            }
            dataStringBuilder.append("&connection_data=");
            try {
                dataStringBuilder.append(URLEncoder.encode(data, "UTF-8"));
            }
            catch (UnsupportedEncodingException e) {
                throw new InvalidArgumentException(
                    "Error during URL encode of your connection data: " +  e.getMessage()
                );
            }
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
}
