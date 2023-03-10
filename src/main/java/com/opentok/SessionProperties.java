/**
 * OpenTok Java SDK
 * Copyright (C) 2023 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.opentok.exception.InvalidArgumentException;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.util.*;


/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#createSession(SessionProperties)} method.
 *
 * @see OpenTok#createSession(com.opentok.SessionProperties properties)
 */
public class SessionProperties {
    private String location;
    private MediaMode mediaMode;
    private ArchiveMode archiveMode;
    private boolean e2ee;

    private SessionProperties(Builder builder) {
        this.location = builder.location;
        this.mediaMode = builder.mediaMode;
        this.archiveMode = builder.archiveMode;
        this.e2ee = builder.e2ee;
    }

    /**
     * Use this class to create a SessionProperties object.
     *
     * @see SessionProperties
     */
    public static class Builder {
        private String location;
        private MediaMode mediaMode = MediaMode.RELAYED;
        private ArchiveMode archiveMode = ArchiveMode.MANUAL;
        private boolean e2ee = false;

        /**
         * Call this method to set an IP address that the OpenTok servers will use to
         * situate the session in its global network. If you do not set a location hint,
         * the OpenTok servers will be based on the first client connecting to the session.
         *
         * @param location The IP address to serve as the locaion hint.
         *
         * @return The SessionProperties.Builder object with the location hint setting.
         */
        public Builder location(String location) throws InvalidArgumentException {
            if (!InetAddressValidator.getInstance().isValidInet4Address(location)) {
                throw new InvalidArgumentException("Location must be a valid IPv4 address. location = " + location);
            }
            this.location = location;
            return this;
        }

       /**
       * Call this method to determine whether the session will transmit streams using the
       * OpenTok Media Router (<code>MediaMode.ROUTED</code>) or not
       * (<code>MediaMode.RELAYED</code>). By default, the <code>mediaMode</code> property
       * is set to <code>MediaMode.RELAYED</code>.
       *
       * <p>
       * With the <code>mediaMode</code> property set to <code>MediaMode.RELAYED</code>, the session
       * will attempt to transmit streams directly between clients. If clients cannot connect due to
       * firewall restrictions, the session uses the OpenTok TURN server to relay audio-video
       * streams.
       *
       * <p>
       * The
       * <a href="https://tokbox.com/developer/guides/create-session/#media-mode" target="_top">
       * OpenTok Media Router</a> provides the following benefits:
       *
       * <ul>
       *   <li>The OpenTok Media Router can decrease bandwidth usage in multiparty sessions.
       *       (When the <code>mediaMode</code> property is set to <code>MediaMode.RELAYED</code>,
       *       each client must send a separate audio-video stream to each client subscribing to
       *       it.)</li>
       *   <li>The OpenTok Media Router can improve the quality of the user experience through
       *     <a href="https://tokbox.com/platform/fallback" target="_top">audio fallback and video
       *     recovery</a>. With these features, if a client's connectivity degrades to a degree that
       *     it does not support video for a stream it's subscribing to, the video is dropped on
       *     that client (without affecting other clients), and the client receives audio only.
       *     If the client's connectivity improves, the video returns.</li>
       *   <li>The OpenTok Media Router supports the
       *     <a href="http://tokbox.com/developer/guides/archiving" target="_top">archiving</a>
       *     feature, which lets you record, save, and retrieve OpenTok sessions.</li>
       * </ul>
       *
       * @param mediaMode Set to a value defined in the {@link MediaMode} enum.
       *
       * @return The SessionProperties.Builder object with the media mode setting.
       */
        public Builder mediaMode(MediaMode mediaMode) {
            this.mediaMode = mediaMode;
            return this;
        }

        /**
         * Call this method to determine whether the session will be automatically archived (<code>ArchiveMode.ALWAYS</code>)
         * or not (<code>ArchiveMode.MANUAL</code>).
         * <p>
         * Using an always archived session also requires the routed media mode (<code>MediaMode.ROUTED</code>).
         *
         * @param archiveMode The Archive mode.
         *
         * @return The SessionProperties.Builder object with the archive mode setting.
         */
        public Builder archiveMode(ArchiveMode archiveMode) {
            this.archiveMode = archiveMode;
            return this;
        }

        /**
         * End-to-end encryption (or E2EE) allows application developers to encrypt media in routed sessions
         * from client to client. Media is already encrypted P2P, client to client, through WebRTC protocols
         * when using a relayed session. This feature adds an encryption layer by encrypting the media payload at the
         * client so that it will remain encrypted through the media server when routing media to other clients.
         * Therefore, you must also set {@link #mediaMode(MediaMode)} to {@linkplain MediaMode#ROUTED} when
         * calling this method.
         *
         * @return The SessionProperties.Builder object with the e2ee property set to {@code true}.
         */
        public Builder endToEndEncryption() {
            this.e2ee = true;
            return this;
        }

        /**
         * Builds the SessionProperties object.
         *
         * @return The SessionProperties object.
         */
        public SessionProperties build() {
            if (this.archiveMode == ArchiveMode.ALWAYS && this.mediaMode != MediaMode.ROUTED) {
                throw new IllegalStateException(
                    "A session with ALWAYS archive mode must also have the ROUTED media mode."
                );
            }
            if (e2ee && mediaMode != MediaMode.ROUTED) {
                throw new IllegalStateException(
                    "A session with RELAYED media mode cannot have end-to-end encryption enabled."
                );
            }
            if (e2ee && archiveMode == ArchiveMode.ALWAYS) {
                throw new IllegalStateException(
                    "A session with ALWAYS archive mode cannot have end-to-end encryption enabled."
                );
            }
            return new SessionProperties(this);
        }
    }

    /**
    * The location hint IP address. See {@link SessionProperties.Builder#location(String location)}.
    */
    public String getLocation() {
        return location;
    }
    
    /**
     * Defines whether the session will transmit streams using the OpenTok Media Server or attempt
     * to transmit streams directly between clients. See
     * {@link SessionProperties.Builder#mediaMode(MediaMode mediaMode)}.
     */
    public MediaMode mediaMode() {
        return mediaMode;
    }

    /**
     * Defines whether the session will be automatically archived (<code>ArchiveMode.ALWAYS</code>)
     * or not (<code>ArchiveMode.MANUAL</code>). See
     * {@link com.opentok.SessionProperties.Builder#archiveMode(ArchiveMode archiveMode)}
     */
    public ArchiveMode archiveMode() {
        return archiveMode;
    }

    /**
     * End-to-end encryption (or E2EE) allows application developers to encrypt media in routed sessions from client
     * to client. Media is already encrypted P2P, client to client, through WebRTC protocols when using a relayed
     * session. This feature adds an encryption layer by encrypting the media payload at the client so that it will
     * remain encrypted through the media server when routing media to other clients.
     *
     * @return {@code true} if end-to-end encryption is enabled, {@code false} otherwise.
     */
    public boolean isEndToEndEncrypted() {
        return e2ee;
    }

    /**
     * Returns the session properties as a Map.
     */
    public Map<String, List<String>> toMap() {
        Map<String, List<String>> params = new HashMap<>();
        if (null != location) {
            ArrayList<String> valueList = new ArrayList<>(1);
            valueList.add(location);
            params.put("location", valueList);
        }

        ArrayList<String> mediaModeValueList = new ArrayList<>(1);
        mediaModeValueList.add(mediaMode.toString());
        params.put("p2p.preference", mediaModeValueList);

        ArrayList<String> archiveModeValueList = new ArrayList<>(1);
        archiveModeValueList.add(archiveMode.toString());
        params.put("archiveMode", archiveModeValueList);

        if (e2ee) {
            ArrayList<String> e2eeValueList = new ArrayList<>(1);
            e2eeValueList.add("" + e2ee);
            params.put("e2ee", e2eeValueList);
        }

        return params;
    }

}
