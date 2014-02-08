/*!
 * OpenTok Java Library
 * http://www.tokbox.com/
 *
 * Copyright 2010, TokBox, Inc.
 */

package com.opentok.api.constants;
/**
 * Defines values for the role parameter of the <code>OpenTokSDK.generateToken()</code> method.
 *
 * @see <a href="../OpenTokSDK.html#generateToken(java.lang.String, java.lang.String)">OpenTokSDK.generateTokentoken(String, String)</a>
 * @see <a href="../OpenTokSDK.html#generateToken(java.lang.String, java.lang.String, java.lang.Long)">OpenTokSDK.generateTokentoken(String, String, Long)</a>
 * @see <a href="../OpenTokSDK.html#generateTokentoken(java.lang.String, java.lang.String, java.lang.Long, java.lang.String)">OpenTokSDK.generateTokentoken(String, String, Long, String)</a>
 */
public class RoleConstants {
    /**
    * The "subscriber" role. The client assigned a token with this role is restricted to subscribing to streams
    * in the session.
    */
    public static final String SUBSCRIBER = "subscriber";
    /**
     * The "publisher" role. The client assigned a token with this role can publish and subscribe to
     * streams in the session.
     */
    public static final String PUBLISHER = "publisher";
    /**
     * The "moderator" role. The client assigned a token with this role can moderate sessions (in
     * clients using the OpenTok JavaScript library) in addition to publishing and subscribing to
     * streams in the session.
     */
    public static final String MODERATOR = "moderator";
}