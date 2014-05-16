package com.opentok;
/**
 * Defines values for the role parameter of the <code>OpenTokSDK.generateToken()</code> method.
 *
 * @see <a href="../OpenTokSDK.html#generateToken(java.lang.String, java.lang.String)">OpenTokSDK.generateTokentoken(String, String)</a>
 * @see <a href="../OpenTokSDK.html#generateToken(java.lang.String, java.lang.String, java.lang.Long)">OpenTokSDK.generateTokentoken(String, String, Long)</a>
 * @see <a href="../OpenTokSDK.html#generateTokentoken(java.lang.String, java.lang.String, java.lang.Long, java.lang.String)">OpenTokSDK.generateTokentoken(String, String, Long, String)</a>
 */
public enum Role {
    SUBSCRIBER,
    PUBLISHER,
    MODERATOR;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
