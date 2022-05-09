/**
 * OpenTok Java SDK
 * Copyright (C) 2022 Vonage.
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

    private BroadcastLayout layout;
    private int maxDuration;
    private boolean hasHls;
    private List<RtmpProperties> rtmpList;
    private String resolution;
    private StreamMode streamMode;
    private Hls hls;

    private BroadcastProperties(Builder builder) {
        this.layout = builder.layout;
        this.maxDuration = builder.maxDuration;
        this.hasHls = builder.hasHls;
        this.hls = builder.hls;
        this.rtmpList = builder.rtmpList;
        this.resolution = builder.resolution;
        this.streamMode = builder.streamMode;
    }

    /**
     * Used to create a BroadcastProperties object.
     *
     * @see BroadcastProperties
     */
    public static class Builder {
        private BroadcastLayout layout = new BroadcastLayout(BroadcastLayout.Type.BESTFIT);
        private int maxDuration = 7200;
        private boolean hasHls = false;

        private Hls hls;
        private List<RtmpProperties> rtmpList = new ArrayList<>();
        private String resolution = "640x480";
        private StreamMode streamMode = StreamMode.AUTO;

        /**
         * Customizes the layout of the broadcast.
         *
         * @param layout An object of type {@link BroadcastLayout}.
         *
         * @return The BroadcastProperties.Builder object with the layout setting.
         */
        public Builder layout(BroadcastLayout layout){
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
        public Builder maxDuration(int maxDuration)  throws InvalidArgumentException {
            if (maxDuration < 60 || maxDuration > 36000) {
                throw new InvalidArgumentException("maxDuration value must be between 60 and 36000 (inclusive).");
            }
            this.maxDuration = maxDuration;
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
         * Call this method to set a list of RTMP broadcast streams. There is a limit of
         * 5 RTMP streams.
         *
         * @param rtmpProps The {@link RtmpProperties} object defining the RTMP streams.
         *
         * @return The BroadcastProperties.Builder object with the list of RtmpProperties setting.
         */
        public Builder addRtmpProperties (RtmpProperties rtmpProps) throws InvalidArgumentException {
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
     * Whether the broadcast has HLS (<code>true</code>) or not (<code>false</code>).
     */
    public boolean hasHls() {
        return hasHls;
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
     * The stream mode of the broadcast.
     */
    public StreamMode streamMode() { return streamMode; }
}
