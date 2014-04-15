package com.opentok.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import com.opentok.api.constants.SessionProperties;
import com.opentok.api.constants.TokenOptions;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.OpenTokInvalidArgumentException;
import com.opentok.exception.OpenTokRequestException;
import com.opentok.util.Base64;
import com.opentok.util.GenerateMac;

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
    
    public int getApiKey() {
        return apiKey;
    }

    public String getSessionId() {
        return sessionId;
    }
    
    public SessionProperties getProperties() {
        return properties;
    }
    
    /**
     * Generates the token for the given session. The role is set to publisher, the token expires in
     * 24 hours, and there is no connection data.
     *
     * @param sessionId The session ID.
     *
     * @see #generateToken(String sessionId, String role, long expireTime, String connectionData)
     */
    public String generateToken() throws OpenTokException {
        // NOTE: maybe there should be a static object for the defaultTokenOptions?
        return this.generateToken(new TokenOptions.Builder().build());
    }

    /**
     * Creates a token for connecting to an OpenTok session. In order to authenticate a user connecting to an OpenTok session
     * that user must pass an authentication token along with the API key.
     *
     * @param role Each role defines a set of permissions granted to the token.
     * Valid values are defined in the RoleConstants class:
     *
     *   * `SUBSCRIBER` &mdash; A subscriber can only subscribe to streams.</li>
     *
     *   * `PUBLISHER` &mdash; A publisher can publish streams, subscribe to streams, and signal.
     *     (This is the default value if you do not specify a value for the `role` parameter.)</li>
     *
     *   * `MODERATOR` &mdash; In addition to the privileges granted to a publisher, a moderator
     *     can call the `forceUnpublish()` and `forceDisconnect()` method of the
     *     Session object.</li>
     *
     * @param expireTime The expiration time, in seconds, since the UNIX epoch. Pass in 0 to use
     * the default expiration time of 24 hours after the token creation time. The maximum expiration
     * time is 30 days after the creation time.
     *
     * @param connectionData A string containing metadata describing the end-user. For example, you can pass the
     * user ID, name, or other data describing the end-user. The length of the string is limited to 1000 characters.
     * This data cannot be updated once it is set.
     *
     * @return The token string.
     */
    public String generateToken(TokenOptions tokenOptions) throws
            OpenTokInvalidArgumentException, OpenTokRequestException {

        if (tokenOptions == null) {
            throw new OpenTokInvalidArgumentException("Token options cannot be null");
        }

        String role = tokenOptions.getRole();
        double expireTime = tokenOptions.getExpireTime(); // will be 0 if nothing was explicitly set
        String data = tokenOptions.getData();             // will be null if nothing was explicitly set

        Long create_time = new Long(System.currentTimeMillis() / 1000).longValue();
        StringBuilder dataStringBuilder = new StringBuilder();
        //Build the string
        Random random = new Random();
        int nonce = random.nextInt();
        dataStringBuilder.append("session_id=");
        dataStringBuilder.append(sessionId);
        dataStringBuilder.append("&create_time=");
        dataStringBuilder.append(create_time);
        dataStringBuilder.append("&nonce=");
        dataStringBuilder.append(nonce);
        dataStringBuilder.append("&role=");
        dataStringBuilder.append(role);

        double now = System.currentTimeMillis() / 1000L;
        if (expireTime == 0) {
            expireTime = now + (60*60*24); // 1 day
        } else if(expireTime < now-1) {
            throw new OpenTokInvalidArgumentException(
                    "Expire time must be in the future. relative time: "+ (expireTime - now));
        } else if(expireTime > (now + (60*60*24*30) /* 30 days */)) {
            throw new OpenTokInvalidArgumentException(
                    "Expire time must be in the next 30 days. too large by "+ (expireTime - (now + (60*60*24*30))));
        }
        dataStringBuilder.append("&expire_time=");
        dataStringBuilder.append(expireTime);

        if (data != null) {
            if(data.length() > 1000) {
                throw new OpenTokInvalidArgumentException(
                        "Connection data must be less than 1000 characters. length: " + data.length());
            }
            dataStringBuilder.append("&connection_data=");
            try {
                dataStringBuilder.append(URLEncoder.encode(data, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new OpenTokInvalidArgumentException(
                        "Error during URL encode of your connection data: " +  e.getMessage());
            }
        }


        StringBuilder tokenStringBuilder = new StringBuilder();
        try {
            tokenStringBuilder.append("T1==");

            StringBuilder innerBuilder = new StringBuilder();
            innerBuilder.append("partner_id=");
            innerBuilder.append(this.apiKey);

            innerBuilder.append("&sig=");

            // TODO: user a more concise crypto routine
            innerBuilder.append(GenerateMac.calculateRFC2104HMAC(dataStringBuilder.toString(),
                    this.apiSecret));
            innerBuilder.append(":");
            innerBuilder.append(dataStringBuilder.toString());

            tokenStringBuilder.append(Base64.encode(innerBuilder.toString()));

        } catch (java.security.SignatureException e) {
            throw new OpenTokRequestException(500, e.getMessage());
        }

        return tokenStringBuilder.toString();
    }
}
