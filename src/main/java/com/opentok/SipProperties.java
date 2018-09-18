/**
 * OpenTok Java SDK
 * Copyright (C) 2018 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#dial(String, String, SipProperties)} method.
 *
 * @see OpenTok#dial(String, String, SipProperties)
 */
public class SipProperties {
    private String sipUri = null;
    private String from = null;
    private String userName = null;
    private String password = null;
    private String headersJsonStartingWithXDash = null;
    private Boolean secure = false;

    private SipProperties(Builder builder) {
        this.sipUri = builder.sipUri;
        this.from = builder.from;
        this.userName = builder.userName;
        this.password = builder.password;
        this.headersJsonStartingWithXDash = builder.headersJsonStartingWithXDash;
        this.secure = builder.secure;
    }

    /**
     * Use this class to create a SipProperties object.
     *
     * @see SipProperties
     */
    public static class Builder {
        private String sipUri = null;
        private String from = null;
        private String userName = null;
        private String password = null;
        private String headersJsonStartingWithXDash = null;
        private boolean secure = false;


        /**
         * Call this method to set the sip uri
         *
         * @param sipUri SIP uri​ ​(required) — The SIP URI to be used as destination of the SIP call
         *  initiated from OpenTok to your SIP platform.
         *  If the SIP ​uri​ contains a ​transport=tls​ header, the negotiation between TokBox and the SIP endpoint will be done securely.
         *               Note that this will only apply to the negotiation itself, and not to the transmission of audio.
         *               If you also audio transmission to be encrypted, set the ​secure​ property to ​true
         * @return The SipProperties.Builder object with the name setting.
         */
        public Builder sipUri(String sipUri) {
            this.sipUri = sipUri;
            return this;
        }
        /**
         * Call this method to set the sip from field (optional)
         *
         * @param from The number or string that will be sent to the final SIP number as the caller.
         *             It must be a string in the form of from@example.com, where from can be a string or a number.
         *             If from is set to a number (for example, "14155550101@example.com"),
         *             it will show up as the incoming number on PSTN phones.
         *             If from is undefined or set to a string (for example, "joe@example.com"),
         *             +00000000 will show up as the incoming number on PSTN phones.
         *
         * @return The SipProperties.Builder object with the name setting.
         */
        public Builder from(String from) {
            this.from = from;
            return this;
        }
        /**
         * Call this method to set the user name of the SIP gateway provider (optional)
         *
         * @param userName Used as a sub field of the auth object in the post JSON.
         *                 set the user name of the SIP gateway provider.
         *
         *
         * @return The SipProperties.Builder object with the name setting.
         */
        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        /**
         * Call this method to set the password of the SIP gateway provider (optional)
         *
         * @param password Used as a sub field of the auth object in the post JSON.
          *                 set the password of the SIP gateway provider.
         *
         * @return The SipProperties.Builder object with the name setting.
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        /**
         * Call this method to  define custom headers to be added to the SIP ​INVITE​
         *
         * @param headersJsonStartingWithXDash his object defines custom headers to be added to the SIP ​INVITE​ request initiated from OpenTok to the your SIP platform.
         *                                    Each of the custom headers must start with the ​"X-"​ prefix, or the call will result in a Bad Request (400) response.
         *
         * @return The SipProperties.Builder object with the name setting.
         */
        public Builder headersJsonStartingWithXDash(String headersJsonStartingWithXDash) {
            this.headersJsonStartingWithXDash = headersJsonStartingWithXDash;
            return this;
        }
        /**
         * Call this method to set the secure flag of the SIP dial
         *
         * @param secure A Boolean flag that indicates whether the media must be transmitted encrypted (​true​) or not (​false​, the default).
         *
         * @return The SipProperties.Builder object with the name setting.
         */
        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }
        /**
         * Builds the SipProperties object.
         *
         * @return The SipProperties object.
         */
        public SipProperties build() {
            return new SipProperties(this);
        }
    }
    /**
     * Returns the sipUri
     */
    public String sipUri() {
        return sipUri;
    }
    /**
     * Returns the from field in the SIP object
     */
    public String from() {
        return from;
    }
    /**
     * Returns the user name field in the SIP object of your SIP provider
     */
    public String userName() {
        return userName;
    }

    /**
     *  Returns the password field in the SIP object of your SIP provider
     */
    public String password() {
        return password;
    }

    /**
     * Returns the JSON SIP headers
     */
    public String headersJsonStartingWithXDash() {
        return headersJsonStartingWithXDash;
    }
    /**
     *  Returns the boolean value of SIP secure field
     */
    public boolean secure() {
        return secure;
    }

}
