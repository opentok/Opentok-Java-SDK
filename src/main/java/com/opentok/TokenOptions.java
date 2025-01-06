/**
 * OpenTok Java SDK
 * Copyright (C) 2025 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.opentok.exception.InvalidArgumentException;

import java.util.List;

/**
 * Defines values for the <code>tokenOptions</code> parameter of the
 * {@link OpenTok#generateToken(String sessionId, TokenOptions tokenOptions)} method
 * and the {@link Session#generateToken(TokenOptions tokenOptions)} method.
 *
 * @see OpenTok#createSession(com.opentok.SessionProperties properties)
 */
public class TokenOptions {

    private Role role;
    private long expireTime;
    private String data;
    private List<String> initialLayoutClassList;
    private boolean legacyT1Token;

    private TokenOptions(Builder builder) {
        role = builder.role != null ? builder.role : Role.PUBLISHER;
        legacyT1Token = builder.legacyT1Token;

        // default value calculated at token generation time
        expireTime = builder.expireTime;

        // default value of null means to omit the key "connection_data" from the token
        data = builder.data;

        // default value of null means to omit the key "initialLayoutClassList" from the token
        initialLayoutClassList = builder.initialLayoutClassList;
    }

    /**
    * Returns the role assigned to the token. See {@link TokenOptions.Builder#role(Role role)}.
    */
    public Role getRole() {
        return role;
    }

    /**
    * Returns the expiration time the token, as the number of seconds since the UNIX epoch.
    * See {@link TokenOptions.Builder#expireTime(long expireTime)}.
    */
    public long getExpireTime() {
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
    * Returns the initial layout class list for streams published by the client using this token.
    * See {@link TokenOptions.Builder#initialLayoutClassList(List initialLayoutClassList)}.
    */
    public List<String> getInitialLayoutClassList() {
        return initialLayoutClassList;
    }

    /**
     * Returns whether the generated token will be in the old T1 format instead of JWT.
     *
     * @return {@code true} if the token will be in the old T1 format, {@code false} otherwise.
     * @since 4.15.0
     */
    public boolean isLegacyT1Token() {
        return legacyT1Token;
    }

    /**
     * Use this class to create a TokenOptions object.
     *
     * @see TokenOptions
     */
    public static class Builder {
        private Role role;
        private long expireTime = 0;
        private String data;
        private List<String> initialLayoutClassList;
        private boolean legacyT1Token = false;

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
         *     publisher, a moderator can perform moderation functions, such as forcing clients to
         *     disconnect, to stop publishing streams, or to mute audio in published streams. See the
         *     <a href="https://tokbox.com/developer/guides/moderation/">Moderation developer guide</a>.
         *     </li>
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
        public Builder expireTime(long expireTime) {
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
        * A List of class names (strings) to be used as the initial layout classes
        * for streams published by the client. Layout classes are used in customizing the layout
        * of videos in
        * <a href="https://tokbox.com/developer/guides/broadcast/live-streaming/">live streaming
        * broadcasts</a> and
        * <a href="https://tokbox.com/developer/guides/archiving/layout-control.html">composed
        * archives</a>. 
        *
        * @param initialLayoutClassList The initial layout class list.
        */
        public Builder initialLayoutClassList (List<String> initialLayoutClassList) {
            this.initialLayoutClassList = initialLayoutClassList;
            return this;
        }

        /**
         * Use this method to generate a legacy T1 token instead of a JWT.
         *
         * @return This builder.
         *
         * @since 4.15.0
         */
        public Builder useLegacyT1Token() {
            legacyT1Token = true;
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
