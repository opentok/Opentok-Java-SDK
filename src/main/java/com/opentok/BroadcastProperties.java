/**
 * OpenTok Java SDK
 * Copyright (C) 2019 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

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
    private String resolution = null;


    private BroadcastProperties(Builder builder) {
        this.layout = builder.layout;
        this.maxDuration = builder.maxDuration;
        this.hasHls = builder.hasHls;
        this.rtmpList = builder.rtmpList;
        this.resolution = builder.resolution;
    }

    /**
     * Use this class to create a BroadcastProperties object.
     *
     * @see BroadcastProperties
     */
    public static class Builder {
        private BroadcastLayout layout = new BroadcastLayout(BroadcastLayout.Type.BESTFIT);
        private int maxDuration = 7200;
        private boolean hasHls = false;
        private List<RtmpProperties> rtmpList = new ArrayList<>();
        private String resolution = "640x480";


        /**
         * Call this method to customize the layout of the broadcast.
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
         * Call this method to set the maximum duration, in seconds, of the broadcast.
         * The broadcast will automatically stop when the maximum duration is reached.
         * You can set the maximum duration to a value from 60 (60 seconds) to 36000 (10 hours).
         * The default maximum duration is 2 hours (7200 seconds).
         *
         * @param maxDuration The maximum duration in seconds.
         *
         * @return The BroadcastProperties.Builder object with the maxDuration setting.
         */
        public Builder maxDuration(int maxDuration)  throws InvalidArgumentException {
            if(maxDuration < 60 || maxDuration > 36000) {
                throw new InvalidArgumentException("maxDuration value must be between 60 and 36000 (inclusive).");
            }
            this.maxDuration = maxDuration;
            return this;
        }

        /**
         * Call this method to include HLS broadcast (<code>true</code>) or not <code>false</code>).
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
         * Call this method to set a list of RTMP broadcast streams. There is a limit of
         * 5 RTMP streams.
         *
         * @param rtmpProps The {@link RtmpProperties} object defining the RTMP streams.
         *
         * @return The BroadcastProperties.Builder object with the list of RtmpProperties setting.
         */
        public Builder addRtmpProperties (RtmpProperties rtmpProps) throws InvalidArgumentException {
            if(this.rtmpList.size() >= 5) {
                throw new InvalidArgumentException("Cannot add more than 5 RtmpProperties properties");
            }
            this.rtmpList.add(rtmpProps);
            return this;
        }

        /**
         * Call this method to set the resolution of the broadcast stream.
         *
         * @param resolution The resolution of the broadcast, either "640x480" (SD, the default) or "1280x720" (HD).
         *
         * @return The BroadcastProperties.Builder object with the resolution setting.
         */
        public Builder resolution(String resolution) {
            this.resolution = resolution;
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
     * The layout of the broadcast session
     */
    public BroadcastLayout layout() {
        return layout;
    }
    /**
     * The max duration in seconds of the broadcast session
     */
    public int maxDuration() {
        return maxDuration;
    }

    /**
     * Whether the broadcast has a HLS  (<code>true</code>) or not (<code>false</code>).
     */
    public boolean hasHls() {
        return hasHls;
    }

    /**
     * Returns the RtmpProperties list.
     */
    public List<RtmpProperties> getRtmpList() {
        return rtmpList;
    }
    /**
     * Returns the resolution of the broadcast
     */
    public String resolution() {
        return resolution;
    }
}
