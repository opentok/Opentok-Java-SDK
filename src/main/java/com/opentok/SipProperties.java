/**
 * OpenTok Java SDK
 * Copyright (C) 2025 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import java.util.Arrays;
import java.util.List;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#dial(String, String, SipProperties)} method.
 *
 * @see OpenTok#dial(String, String, SipProperties)
 */
public class SipProperties {
    private String sipUri;
    private String from;
    private String userName;
    private String password;
    private String headersJsonStartingWithXDash;
    private Boolean secure;
    private Boolean video;
    private Boolean observeForceMute;
    private String[] streams;

    private SipProperties(Builder builder) {
        sipUri = builder.sipUri;
        from = builder.from;
        userName = builder.userName;
        password = builder.password;
        headersJsonStartingWithXDash = builder.headersJsonStartingWithXDash;
        secure = builder.secure;
        video = builder.video;
        observeForceMute = builder.observeForceMute;
        streams = builder.streams;
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
        private boolean video = false;
        private boolean observeForceMute = false;
        private String[] streams = null;

        /**
         * Call this method to set the SIP URI.
         *
         * @param sipUri ​(required) &mdash; The SIP URI to be used as destination of the SIP call
         *  initiated from OpenTok to your SIP platform. If the SIP URI contains a ​
         *  transport=tls​ header, the negotiation between OpenTok and the SIP endpoint will be 
         *  done securely. Note that this will only apply to the negotiation itself, and not to the 
         *  transmission of audio. If you also audio transmission to be encrypted, set the
         *  <code>​secure​</code> property to ​true
         *
         * @return The SipProperties.Builder object with the SIP URI set.
         */
        public Builder sipUri(String sipUri) {
            this.sipUri = sipUri;
            return this;
        }

        /**
         * Call this method to set the SIP <code>from</code> field (optional).
         *
         * @param from The number or string that will be sent to the final SIP number as the caller.
         *   It must be a string in the form of from@example.com, where from can be a string or
         *   a number. If from is set to a number (for example, "14155550101@example.com"),
         *   it will show up as the incoming number on PSTN phones.
         *   If from is undefined or set to a string (for example, "joe@example.com"),
         *   +00000000 will show up as the incoming number on PSTN phones.
         *
         * @return The SipProperties.Builder object with the from number set.
         */
        public Builder from(String from) {
            this.from = from;
            return this;
        }

        /**
         * Call this method to set the username for the SIP gateway provider (optional).
         *
         * @param userName The username.
         *
         * @return The SipProperties.Builder object with the username set.
         */
        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        /**
         * Call this method to set the password for the SIP gateway provider (optional).
         *
         * @param password The password.
         *
         * @return The SipProperties.Builder object with the password set.
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Call this method to define custom headers to be added to the SIP ​INVITE​
         * initiated from OpenTok to your SIP platform.
         *
         * @param headersJsonStartingWithXDash This JSON string defines custom headers
         * to be added to the SIP ​INVITE​ request initiated from OpenTok to your SIP platform.
         * <p>
         * <b>Note:</b> You no longer need to append the ​"X-"​ prefix to the beginning of
         * custom headers. This restriction, which applied previously, has been removed.
         *
         * @return The SipProperties.Builder object.
         */
        public Builder headersJsonStartingWithXDash(String headersJsonStartingWithXDash) {
            this.headersJsonStartingWithXDash = headersJsonStartingWithXDash;
            return this;
        }

        /**
         * Call this method and pass in <code>true</code> to indicate that the media
         * must be transmitted encrypted. Pass in <code>false​</code>, the default, if encryption
         * is not required.
         *
         * @param secure A Boolean flag that indicates whether the media must be transmitted
         * encrypted (​true​) or not (​false​, the default).
         *
         * @return The SipProperties.Builder object with the media security setting.
         */
        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        /**
         * Call this method and pass in <code>true</code> to enable video in the SIP call.
         * The default is <code>false</code>.
         *
         * @param video Whether video should be enabled in the SIP call.
         *
         * @return The SipProperties.Builder object with the SIP video setting.
         */
        public Builder video(boolean video) {
            this.video = video;
            return this;
        }

        /**
         * Call this method and pass in <code>true</code> to have the SIP end point observe
         * <a href="https://tokbox.com/developer/guides/moderation/#force_mute">force mute moderation</a>.
         * The default is <code>false</code>.
         *
         * @param observeForceMute Whether to observe force mute moderation.
         *
         * @return The SipProperties.Builder object with the observeForceMute setting.
         */
        public Builder observeForceMute(boolean observeForceMute) {
            this.observeForceMute = observeForceMute;
            return this;
        }

        /**
         * The stream IDs of the participants' which will be subscribed by the SIP participant.
         * If not provided, all streams in session will be selected.
         *
         * @param streams Stream IDs to select.
         *
         * @return The SipProperties.Builder object with the streams setting.
         */
        public Builder streams(String... streams) {
            this.streams = streams;
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
     * Returns the SIP URI.
     */
    public String sipUri() {
        return sipUri;
    }

    /**
     * Returns the from value.
     */
    public String from() {
        return from;
    }

    /**
     * Returns the user name.
     */
    public String userName() {
        return userName;
    }

    /**
     *  Returns the password.
     */
    public String password() {
        return password;
    }

    /**
     * Returns the SIP headers as JSON.
     */
    public String headersJsonStartingWithXDash() {
        return headersJsonStartingWithXDash;
    }

    /**
     *  Returns the secure value (<code>true</code> or <code>false</code>).
     */
    public boolean secure() {
        return secure;
    }

    /**
     * Return the video value (<code>true</code> or <code>false</code>).
     */
    public boolean video() {
        return video;
    }

    /**
     * Returns the observeForceMute value (<code>true</code> or <code>false</code>).
     */
    public boolean observeForceMute() {
        return observeForceMute;
    }

    /**
     * Returns the subscribed stream IDs.
     *
     * @return The selected stream IDs as an array.
     */
    public String[] streams() {
        return streams;
    }
}
