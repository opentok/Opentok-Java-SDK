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
    private String location, archiveName;
    private MediaMode mediaMode;
    private ArchiveMode archiveMode;
    private Resolution archiveResolution;
    private boolean e2ee;

    private SessionProperties(Builder builder) {
        location = builder.location;
        mediaMode = builder.mediaMode;
        archiveMode = builder.archiveMode;
        e2ee = builder.e2ee;
        archiveName = builder.archiveName;
        archiveResolution = builder.archiveResolution;
    }

    /**
     * Use this class to create a SessionProperties object.
     *
     * @see SessionProperties
     */
    public static class Builder {
        private String location, archiveName;
        private MediaMode mediaMode = MediaMode.RELAYED;
        private ArchiveMode archiveMode = ArchiveMode.MANUAL;
        private Resolution archiveResolution;
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
         * Indicates the archive resolution for all the archives in auto archived session. A session that begins with
         * archive mode {@link ArchiveMode#ALWAYS} will use this resolution for all archives of that session.
         *
         * @param archiveResolution The auto archive resolution as an enum.
         *
         * @return The SessionProperties.Builder object with the archive resolution setting.
         */
        public Builder archiveResolution(Resolution archiveResolution) {
            this.archiveResolution = archiveResolution;
            return this;
        }

        /**
         * Indicates the archive name for all the archives in auto archived session. A session that begins with
         * archive mode {@link ArchiveMode#ALWAYS} will use this archive name for all archives of that session.
         *
         * @param archiveName The archive name, maximum 80 characters in length.
         *
         * @return The SessionProperties.Builder object with the archive name setting.
         */
        public Builder archiveName(String archiveName) {
            this.archiveName = archiveName;
            return this;
        }

        /**
         * Enables <a href="https://tokbox.com/developer/guides/end-to-end-encryption">end-to-end encryption</a> for a routed session.
         * You must also set {@link #mediaMode(MediaMode)} to {@linkplain MediaMode#ROUTED} when
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
            if (archiveMode == ArchiveMode.ALWAYS && mediaMode != MediaMode.ROUTED) {
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
            if (archiveMode == ArchiveMode.MANUAL) {
                if (archiveResolution != null) {
                    throw new IllegalStateException("Resolution cannot be set for manual archives.");
                }
                if (archiveName != null) {
                    throw new IllegalStateException("Name cannot be set for manual archives.");
                }
            }
            if (archiveName != null && (archiveName.trim().length() < 1 || archiveName.length() > 80)) {
                throw new IllegalArgumentException("Archive name must be between 1 and 80 characters.");
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
     * Indicates the archive resolution for all the archives in auto archived session. A session that begins with
     * archive mode {@link ArchiveMode#ALWAYS} will use this resolution for all archives of that session.
     *
     * @return The archive name, or {@code null} if not set (the default).
     */
    public String archiveName() {
        return archiveName;
    }

    /**
     * Indicates the archive resolution for all the archives in auto archived session. A session that begins with
     * archive mode {@link ArchiveMode#ALWAYS} will use this resolution for all archives of that session.
     *
     * @return The archive resolution enum, or {@code null} if not set (the default).
     */
    public Resolution archiveResolution() {
        return archiveResolution;
    }

    /**
     * Defines whether the session will use
     * <a href="https://tokbox.com/developer/guides/end-to-end-encryption">end-to-end encryption</a>.
     * See {@link com.opentok.SessionProperties.Builder#endToEndEncryption()}.
     *
     * @return {@code true} if end-to-end encryption is enabled, {@code false} otherwise.
     */
    public boolean isEndToEndEncrypted() {
        return e2ee;
    }

    /**
     * Serializes the properties for making a request.
     *
     * @return The session properties as a Map.
     */
    public Map<String, List<String>> toMap() {
        Map<String, List<String>> params = new HashMap<>();

        if (location != null) {
            ArrayList<String> valueList = new ArrayList<>(1);
            valueList.add(location);
            params.put("location", valueList);
        }
        if (mediaMode != null) {
            ArrayList<String> mediaModeValueList = new ArrayList<>(1);
            mediaModeValueList.add(mediaMode.toString());
            params.put("p2p.preference", mediaModeValueList);
        }
        if (archiveMode != null) {
            ArrayList<String> archiveModeValueList = new ArrayList<>(1);
            archiveModeValueList.add(archiveMode.toString());
            params.put("archiveMode", archiveModeValueList);
        }
        if (archiveResolution != null) {
            ArrayList<String> archiveResolutionValueList = new ArrayList<>(1);
            archiveResolutionValueList.add(archiveResolution.toString());
            params.put("archiveResolution", archiveResolutionValueList);
        }
        if (archiveName != null) {
            ArrayList<String> archiveNameValueList = new ArrayList<>(1);
            archiveNameValueList.add(archiveName);
            params.put("archiveName", archiveNameValueList);
        }
        if (e2ee) {
            ArrayList<String> e2eeValueList = new ArrayList<>(1);
            e2eeValueList.add(String.valueOf(e2ee));
            params.put("e2ee", e2eeValueList);
        }

        if (e2ee) {
            ArrayList<String> e2eeValueList = new ArrayList<>(1);
            e2eeValueList.add("" + e2ee);
            params.put("e2ee", e2eeValueList);
        }

        return params;
    }

}
