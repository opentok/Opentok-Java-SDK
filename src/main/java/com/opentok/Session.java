/**
 * OpenTok Java SDK
 * Copyright (C) 2016 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.opentok.exception.OpenTokException;
import com.opentok.util.TokenGenerator;

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
        this.sessionId = sessionId;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.properties = new SessionProperties.Builder().build();
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
        // NOTE: maybe there should be a static object for the defaultTokenOptions?
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
        return TokenGenerator.generateToken(sessionId, tokenOptions, apiKey, apiSecret);
    }
}
