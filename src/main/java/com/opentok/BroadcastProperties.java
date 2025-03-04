/**
 * OpenTok Java SDK
 * Copyright (C) 2025 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.opentok.Broadcast.StreamMode;
import com.opentok.exception.InvalidArgumentException;

import java.util.ArrayList;
import java.util.List;


/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#startBroadcast(String sessionId, BroadcastProperties properties)} method.
 */
public class BroadcastProperties {
    private final BroadcastLayout layout;
    private final int maxDuration;
    private final int maxBitrate;
    private final boolean hasHls;
    private final boolean hasAudio;
    private final boolean hasVideo;
    private final List<RtmpProperties> rtmpList;
    private final String resolution;
    private final String multiBroadcastTag;
    private final StreamMode streamMode;
    private final Hls hls;

    private BroadcastProperties(Builder builder) {
        layout = builder.layout;
        maxDuration = builder.maxDuration;
        maxBitrate = builder.maxBitrate;
        hasHls = builder.hasHls;
        hasAudio = builder.hasAudio;
        hasVideo = builder.hasVideo;
        hls = builder.hls;
        rtmpList = builder.rtmpList;
        resolution = builder.resolution;
        streamMode = builder.streamMode;
        multiBroadcastTag = builder.multiBroadcastTag;
    }

    /**
     * Used to create a BroadcastProperties object.
     *
     * @see BroadcastProperties
     */
    public static class Builder {
        private BroadcastLayout layout = new BroadcastLayout(BroadcastLayout.Type.BESTFIT);
        private int maxDuration = 7200;
        private int maxBitrate = 2_000_000;
        private boolean hasHls = false;
        private boolean hasAudio = true;
        private boolean hasVideo = true;
        private String multiBroadcastTag;
        private Hls hls;
        private final List<RtmpProperties> rtmpList = new ArrayList<>(5);
        private String resolution = "640x480";
        private StreamMode streamMode = StreamMode.AUTO;

        /**
         * Customizes the layout of the broadcast.
         *
         * @param layout An object of type {@link BroadcastLayout}.
         *
         * @return The BroadcastProperties.Builder object with the layout setting.
         */
        public Builder layout(BroadcastLayout layout) {
            this.layout = layout;
            return this;
        }

        /**
         * Sets the maximum duration, in seconds, of the broadcast.
         * The broadcast will automatically stop when the maximum duration is reached.
         * You can set the maximum duration to a value from 60 (60 seconds) to 36000 (10 hours).
         * The default maximum duration is 2 hours (7200 seconds).
         *
         * @param maxDuration The maximum duration in seconds.
         *
         * @return The BroadcastProperties.Builder object with the maxDuration setting.
         */
        public Builder maxDuration(int maxDuration) throws InvalidArgumentException {
            if (maxDuration < 60 || maxDuration > 36_000) {
                throw new InvalidArgumentException("maxDuration value must be between 60 and 36000 (inclusive).");
            }
            this.maxDuration = maxDuration;
            return this;
        }

        /**
         * Sets the maximum bitrate in bits per second for broadcast composing.
         *
         * @param maxBitrate The maximum bitrate in bits per second.
         *
         * @return The BroadcastProperties.Builder object with the maxBitrate setting.
         *
         * @throws InvalidArgumentException If the bitrate is out of bounds.
         */
        public Builder maxBitrate(int maxBitrate) throws InvalidArgumentException {
            if (maxBitrate < 100_000 || maxBitrate > 6_000_000) {
                throw new InvalidArgumentException("maxBitrate value must be between 100_000 and 6_000_000.");
            }
            this.maxBitrate = maxBitrate;
            return this;
        }

        /**
         * Call this method to include an HLS broadcast (<code>true</code>) or not <code>false</code>).
         *
         * @param hasHls Whether the HLS broadcast is enabled or not.
         *
         * @return The BroadcastProperties.Builder object with the HLS setting.
         */
        public Builder hasHls(boolean hasHls) {
            this.hasHls = hasHls;
            return this;
        }

        /**
         * Call this method to include HLS options. This will set <code>hasHls</code> to <code>true</code>.
         *
         * @param hls The HLS object.
         *
         * @return The BroadcastProperties.Builder object with HLS populated.
         */
        public Builder hls(Hls hls) {
            return hasHls((this.hls = hls) != null);
        }

        /**
         * Whether to include audio in the broadcast ({@code true} by default).
         *
         * @param hasAudio {@code true} if audio should be included, {@code false} otherwise.
         *
         * @return The BroadcastProperties.Builder object with the hasAudio setting.
         */
        public Builder hasAudio(boolean hasAudio) {
            this.hasAudio = hasAudio;
            return this;
        }

        /**
         * Whether to include video in the broadcast ({@code true} by default).
         *
         * @param hasVideo {@code true} if video should be included, {@code false} otherwise.
         *
         * @return The BroadcastProperties.Builder object with the hasVideo setting.
         */
        public Builder hasVideo(boolean hasVideo) {
            this.hasVideo = hasVideo;
            return this;
        }

        /**
         * Call this method to set a list of RTMP broadcast streams. There is a limit of
         * 5 RTMP streams.
         *
         * @param rtmpProps The {@link RtmpProperties} object defining the RTMP streams.
         *
         * @return The BroadcastProperties.Builder object with the list of RtmpProperties setting.
         */
        public Builder addRtmpProperties(RtmpProperties rtmpProps) throws InvalidArgumentException {
            if (this.rtmpList.size() >= 5) {
                throw new InvalidArgumentException("Cannot add more than 5 RtmpProperties properties");
            }
            this.rtmpList.add(rtmpProps);
            return this;
        }

        /**
         * Sets the resolution of the broadcast stream.
         *
         * @param resolution The resolution of the broadcast, either "640x480" (SD, the default),
         *                  "1280x720" (HD) or "1920x1080" (FHD).
         *
         * @return The BroadcastProperties.Builder object with the resolution setting.
         */
        public Builder resolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        /**
         * Sets the stream mode for this broadcast
         *
         * When streams are selected automatically (<code>StreamMode.AUTO</code>, the default), all
         * streams in the session can be included in the archive. When streams are selected manually
         * (<code>StreamMode.MANUAL</code>), you specify streams to be included based on calls
         * to the {@link OpenTok#addBroadcastStream(String, String, boolean, boolean)} and
         * {@link OpenTok#removeBroadcastStream(String, String)} methods. With
         * <code>StreamMode.MANUAL</code>, you can specify whether a stream's audio, video, or both
         * are included in the archive. Un both automatic and manual modes, the archive composer
         * includes streams based on
         * <a href="https://tokbox.com/developer/guides/archive-broadcast-layout/#stream-prioritization-rules">stream
         * prioritization rules</a>.
         *
         * @param streamMode Set to a value defined in the {@link Broadcast.StreamMode} enum.
         *
         * @return The BroadcastProperties.Builder object with the stream mode string.
         */
        public Builder streamMode(StreamMode streamMode) {
            this.streamMode = streamMode;
            return this;
        }

        /**
         * Set this to support multiple broadcasts for the same session simultaneously.
         * Set this to a unique string for each simultaneous broadcast of an ongoing session. See
         * <a href="https://tokbox.com/developer/guides/broadcast/live-streaming#simultaneous-broadcasts">
         * Simultaneous Broadcasts documentation</a>.
         *
         * @param multiBroadcastTag A unique multi-broadcast tag.
         *
         * @return The BroadcastProperties.Builder object with the multiBroadcastTag setting.
         */
        public Builder multiBroadcastTag(String multiBroadcastTag) {
            this.multiBroadcastTag = multiBroadcastTag;
            return this;
        }

         /**
         * Builds the BroadcastProperties object.
         *
         * @return The BroadcastProperties object.
         */
        public BroadcastProperties build() {
            return new BroadcastProperties(this);
        }
    }


    /**
     * The layout of the broadcast.
     */
    public BroadcastLayout layout() {
        return layout;
    }

    /**
     * The maximum duration in seconds of the broadcast.
     */
    public int maxDuration() {
        return maxDuration;
    }

    /**
     * The maximum bitrate in bits per second of the broadcast.
     */
    public int maxBitrate() {
        return maxBitrate;
    }

    /**
     * Whether the broadcast has HLS (<code>true</code>) or not (<code>false</code>).
     */
    public boolean hasHls() {
        return hasHls;
    }

    /**
     * Whether the broadcast has audio (<code>true</code>) or not (<code>false</code>).
     */
    public boolean hasAudio() {
        return hasAudio;
    }

    /**
     * Whether the broadcast has video (<code>true</code>) or not (<code>false</code>).
     */
    public boolean hasVideo() {
        return hasVideo;
    }

    /**
     * The HLS configuration object, or <code>null</code> if {@link BroadcastProperties#hasHls} is false.
     */
    public Hls hls() {
        return hls;
    }

    /**
     * Returns the RtmpProperties list.
     */
    public List<RtmpProperties> rtmpList() {
        return rtmpList;
    }

    /**
     * Returns the resolution of the broadcast.
     */
    public String resolution() {
        return resolution;
    }

    /**
     * Returns the multiBroadcastTag, if present.
     */
    public String getMultiBroadcastTag() {
        return multiBroadcastTag;
    }

    /**
     * The stream mode of the broadcast.
     */
    public StreamMode streamMode() {
        return streamMode;
    }
}
