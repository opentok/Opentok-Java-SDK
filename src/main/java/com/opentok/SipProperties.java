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
 * {@link OpenTok#createSession(SessionProperties)} method.
 *
 * @see OpenTok#createSession(com.opentok.SessionProperties properties)
 */
public class SipProperties {
    private String sipUri = null;
    private String from = null;
    private String userName = null;
    private String password = null;
    private String jsonHeadersStartingWithXDash = null;
    private Boolean secure = false;

    private SipProperties(Builder builder) {
        this.sipUri = builder.sipUri;
        this.from = builder.from;
        this.userName = builder.userName;
        this.password = builder.password;
        this.jsonHeadersStartingWithXDash = builder.jsonHeadersStartingWithXDash;
        this.secure = builder.secure;
    }

    /**
     * Use this class to create a ArchiveProperties object.
     *
     * @see ArchiveProperties
     */
    public static class Builder {
        private String sipUri = null;
        private String from = null;
        private String userName = null;
        private String password = null;
        private String jsonHeadersStartingWithXDash = null;
        private boolean secure = false;


        /**
         * Call this method to set the sip uri
         *
         * @param sipUri The name of the archive. You can use this name to identify the archive. It is a property
         * of the Archive object, and it is a property of archive-related events in the OpenTok JavaScript SDK.
         *
         * @return The ArchiveProperties.Builder object with the name setting.
         */
        public Builder sipUri(String sipUri) {
            this.sipUri = sipUri;
            return this;
        }
        /**
         * Call this method to set the sip uri
         *
         * @param from The name of the archive. You can use this name to identify the archive. It is a property
         * of the Archive object, and it is a property of archive-related events in the OpenTok JavaScript SDK.
         *
         * @return The ArchiveProperties.Builder object with the name setting.
         */
        public Builder from(String from) {
            this.from = from;
            return this;
        }
        /**
         * Call this method to set a name to the archive.
         *
         * @param userName The resolution of the archive, either "640x480" (SD, the default) or "1280x720" (HD).
         * This property only applies to composed archives.
         * If you set this property and set the outputMode property to "individual", the call in the API method results in an error.
         *
         * @return The ArchiveProperties.Builder object with the name setting.
         */
        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        /**
         * Call this method to set a name to the archive.
         *
         * @param password The resolution of the archive, either "640x480" (SD, the default) or "1280x720" (HD).
         * This property only applies to composed archives.
         * If you set this property and set the outputMode property to "individual", the call in the API method results in an error.
         *
         * @return The ArchiveProperties.Builder object with the name setting.
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        /**
         * Call this method to set a name to the archive.
         *
         * @param jsonHeadersStartingWithXDash The resolution of the archive, either "640x480" (SD, the default) or "1280x720" (HD).
         * This property only applies to composed archives.
         * If you set this property and set the outputMode property to "individual", the call in the API method results in an error.
         *
         * @return The ArchiveProperties.Builder object with the name setting.
         */
        public Builder jsonHeadersStartingWithXDash(String jsonHeadersStartingWithXDash) {
            this.jsonHeadersStartingWithXDash = jsonHeadersStartingWithXDash;
            return this;
        }
        /**
         * Call this method to set a name to the archive.
         *
         * @param secure The resolution of the archive, either "640x480" (SD, the default) or "1280x720" (HD).
         * This property only applies to composed archives.
         * If you set this property and set the outputMode property to "individual", the call in the API method results in an error.
         *
         * @return The ArchiveProperties.Builder object with the name setting.
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
     * Returns the name of the archive, which you can use to identify the archive
     */
    public String sipUri() {
        return sipUri;
    }
    /**
     * Returns the name of the archive, which you can use to identify the archive
     */
    public String from() {
        return from;
    }
    /**
     * Returns the resolution of the archive
     */
    public String userName() {
        return userName;
    }

    /**
     * Whether the archive has a video track (<code>true</code>) or not (<code>false</code>).
     */
    public String password() {
        return password;
    }

    /**
     * Whether the archive has a video track (<code>true</code>) or not (<code>false</code>).
     */
    public String jsonHeadersStartingWithXDash() {
        return jsonHeadersStartingWithXDash;
    }
    /**
     * Whether the archive has an audio track (<code>true</code>) or not (<code>false</code>).
     */
    public boolean secure() {
        return secure;
    }

}
