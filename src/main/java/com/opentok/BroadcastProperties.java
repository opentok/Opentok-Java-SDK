/**
 * OpenTok Java SDK
 * Copyright (C) 2018 TokBox, Inc.
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
 * {@link OpenTok#createSession(SessionProperties)} method.
 *
 * @see OpenTok#createSession(com.opentok.SessionProperties properties)
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
         * Call this method to customize the layout of the broadcast
         *
         * @param layout An object of type {@link BroadcastLayout} .
         *
         * @return The BroadcastProperties.Builder object with the layout setting.
         */
        public Builder layout(BroadcastLayout layout){
            this.layout = layout;
            return this;
        }
        /**
         * Call this method to set the time duration, in seconds, of the broadcast . The default is 7200 seconds
         *
         * @param maxDuration The maximum time duration in seconds
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
         * Call this method to set rtmp list of the broadcast stream. A limit of 5 BroadcastProperties.RtmpProperties object is enforced.
         *
         * @param rtmpProps The rtmp properties object .
         *
         * @return The BroadcastProperties.Builder object with the list of BroadcastProperties.RtmpProperties setting.
         */
        public Builder addRtmpProperties (BroadcastProperties.RtmpProperties rtmpProps) throws InvalidArgumentException {
            if(this.rtmpList.size() >= 5) {
                throw new InvalidArgumentException("Cannot add more than 5 BroadcastProperties.RtmpProperties properties");
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
     * Defines values for the <code>BroadcastProperties</code> parameter of the
     * {@link OpenTok#startBroadcast(String, BroadcastProperties)}
     *
     * @see OpenTok#startBroadcast(String, BroadcastProperties)
     */
    public static class RtmpProperties {
        private String id = null;
        private String serverUrl = null;
        private String streamName = null;

        private RtmpProperties(RtmpProperties.Builder builder) {
            this.id = builder.id;
            this.serverUrl = builder.serverUrl;
            this.streamName = builder.streamName;
        }
        /**
         * Use this class to create a BroadcastProperties.RtmpProperties object.
         *
         * @see com.opentok.BroadcastProperties.RtmpProperties
         */
        public static class Builder {
            private String id = null;
            private String serverUrl = null;
            private String streamName = null;

            /**
             * Call this method to set a type of the signal.
             *
             * @param id The stream ID
             *
             * @return The BroadcastProperties.RtmpProperties.Builder object with the id setting.
             */
            public  RtmpProperties.Builder id(String id) {
                this.id = id;
                return this;
            }

            /**
             * Call this method to set the RTMP server URL.
             *
             * @param serverUrl The RTMP server URL
             *
             * @return The BroadcastProperties.RtmpProperties.Builder object with the the RTMP server URL setting.
             */
            public  RtmpProperties.Builder serverUrl(String serverUrl) {
                this.serverUrl = serverUrl;
                return this;
            }
            /**
             * Call this method to set the stream name, such as the YouTube Live stream name or the Facebook stream key.
             *
             * @param streamName The stream name
             *
             * @return The BroadcastProperties.RtmpProperties.Builder object with the stream name setting.
             */
            public  RtmpProperties.Builder streamName(String streamName) {
                this.streamName = streamName;
                return this;
            }
            /**
             * Builds the BroadcastProperties.RtmpProperties object.
             *
             * @return The BroadcastProperties.RtmpProperties object.
             */
            public RtmpProperties build() {
                return new RtmpProperties(this);
            }
        }
        /**
         * Returns the id of the broadcast stream
         */
        public String id() {
            return id;
        }
        /**
         *   Returns the rtmp server url of the broadcast stream
         */
        public String serverUrl() {
            return serverUrl;
        }

        /**
         *   Returns the stream name of the broadcast, such as the YouTube Live stream name or the Facebook stream key.
         */
        public String streamName() {
            return streamName;
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
     * Returns the BroadcastProperties.RtmpProperties list.
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
