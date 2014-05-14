package com.opentok;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Random;

import com.opentok.exception.InvalidArgumentException;
import com.opentok.util.Crypto;
import org.apache.commons.codec.binary.Base64;

import com.opentok.exception.OpenTokException;

/**
* Represents an OpenTok session. Use the {@link OpenTok#createSession(SessionProperties properties)}
* method to create an OpenTok session. Use the {@link #getSessionId()} method of the session object
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
        Long create_time = new Long(System.currentTimeMillis() / 1000).longValue();

        StringBuilder dataStringBuilder = new StringBuilder();
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
            throw new InvalidArgumentException(
                    "Expire time must be in the future. relative time: "+ (expireTime - now));
        } else if(expireTime > (now + (60*60*24*30) /* 30 days */)) {
            throw new InvalidArgumentException(
                    "Expire time must be in the next 30 days. too large by "+ (expireTime - (now + (60*60*24*30))));
        }
        // NOTE: Double.toString() would print the value with scientific notation
        dataStringBuilder.append(String.format("&expire_time=%.0f", expireTime));

        if (data != null) {
            if(data.length() > 1000) {
                throw new InvalidArgumentException(
                        "Connection data must be less than 1000 characters. length: " + data.length());
            }
            dataStringBuilder.append("&connection_data=");
            try {
                dataStringBuilder.append(URLEncoder.encode(data, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new InvalidArgumentException(
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

            innerBuilder.append(Crypto.signData(dataStringBuilder.toString(), this.apiSecret));
            innerBuilder.append(":");
            innerBuilder.append(dataStringBuilder.toString());

            tokenStringBuilder.append(
                    Base64.encodeBase64String(
                            innerBuilder.toString().getBytes("UTF-8")
                    )
                    .replace("+", "-")
                    .replace("/", "_")
            );

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (SignatureException e) {
            throw new OpenTokException("Could not generate token, a signing error occurred.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new OpenTokException("Could not generate token, a signing error occurred.", e);
        } catch (InvalidKeyException e) {
            throw new OpenTokException("Could not generate token, a signing error occurred.", e);
        } catch (UnsupportedEncodingException e) {
            throw new OpenTokException("Could not generate token, a signing error occurred.", e);
        }

        return tokenStringBuilder.toString();
    }
}
