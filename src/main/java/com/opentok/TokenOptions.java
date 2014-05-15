package com.opentok;

import com.opentok.exception.InvalidArgumentException;

/**
 * Defines values for the <code>tokenOptions</code> parameter of the
 * {@link OpenTok#generateToken(String sessionId, TokenOptions tokenOptions)} method
 * and the {@link Session#generateToken(TokenOptions tokenOptions)} method.
 *
 * @see OpenTok#createSession(com.opentok.SessionProperties properties)
 */
public class TokenOptions {

    private Role role;
    private double expireTime;
    private String data;

    private TokenOptions(Builder builder) {
        this.role = builder.role != null ? builder.role : Role.PUBLISHER;

        // default value calculated at token generation time
        this.expireTime = builder.expireTime;

        // default value of null means to omit the key "connection_data" from the token
        this.data = builder.data;
    }

    /**
    * Returns the role assigned to the token. See {@link TokenOptions.Builder#role(Role role)}.
    */
    public Role getRole() {
        return role;
    }

    /**
    * Returns the expiration time the token, as the number of seconds since the UNIX epoch.
    * See {@link TokenOptions.Builder#expireTime(double expireTime)}.
    */
    public double getExpireTime() {
        return expireTime;
    }

    /**
    * Returns the connection metadata assigned to the token. See
    * {@link TokenOptions.Builder#data(String data)}.
    */
    public String getData() {
        return data;
    }

    /**
     * Use this class to create a TokenOptions object.
     *
     * @see TokenOptions
     */
    public static class Builder {
        private Role role;
        private double expireTime = 0;
        private String data;

        /**
         * Sets the role for the token. Each role defines a set of permissions granted to the token.
         *
         * @param role The role for the token. Valid values are defined in the Role class:
         * <ul>
         *   <li> <code>SUBSCRIBER</code> &mdash; A subscriber can only subscribe to streams.</li>
         *
         *   <li> <code>PUBLISHER</code> &mdash; A publisher can publish streams, subscribe to
         *      streams, and signal. (This is the default value if you do not specify a role.)</li>
         *
         *   <li> <code>MODERATOR</code> &mdash; In addition to the privileges granted to a
         *     publisher, in clients using the OpenTok.js 2.2 library, a moderator can call the
         *     <code>forceUnpublish()</code> and <code>forceDisconnect()</code> method of the
         *     Session object.</li>
         * </ul>
         */
        public Builder role(Role role) {
            this.role = role;
            return this;
        }

         /**
         * Sets the expiration time for the token.
         *
         * @param expireTime The expiration time, in seconds since the UNIX epoch. Pass in 0 to use
         * the default expiration time of 24 hours after the token creation time. The maximum
         * expiration time is 30 days after the creation time.
         */
        public Builder expireTime(double expireTime) {
            // NOTE: since this object can be stored/cached, validation should occur at token generation time
            this.expireTime = expireTime;
            return this;
        }

         /**
         * A string containing connection metadata describing the end-user. For example, you
         * can pass the user ID, name, or other data describing the end-user. The length of the
         * string is limited to 1000 characters. This data cannot be updated once it is set.
         *
         * @param data The connection metadata.
         */
        public Builder data(String data) throws InvalidArgumentException {
            if (data.length() <= 1000) {
                this.data = data;
            } else {
                throw new InvalidArgumentException(
                        "The given connection data is too long, limit is 1000 characters: " + data.length());
            }
            return this;
        }

        /**
         * Builds the TokenOptions object.
         *
         * @return The TokenOptions object.
         */
        public TokenOptions build() {
            return new TokenOptions(this);
        }
    }

}
