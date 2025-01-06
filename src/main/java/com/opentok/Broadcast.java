/**
 * OpenTok Java SDK
 * Copyright (C) 2025 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a live streaming broadcast of an OpenTok session.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Broadcast {
    /**
     * Defines values used in the
     * {@link BroadcastProperties.Builder#streamMode(com.opentok.Broadcast.StreamMode)} method
     * and returned by the {@link Broadcast#getStreamMode()} method.
     */
    public enum StreamMode {
        /**
        * Streams will be automatically included in the broadcast.
         */
        AUTO,
        /**
         * Streams will be included in the archive based on calls to the
         * {@link OpenTok#addBroadcastStream(String, String, boolean, boolean)} and
         * {@link OpenTok#removeBroadcastStream(String, String)} methods.
         */
        MANUAL;

        @JsonValue
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    @JsonProperty private String id;
    @JsonProperty private String sessionId;
    @JsonProperty private int projectId;
    @JsonProperty private long createdAt;
    @JsonProperty private long updatedAt;
    @JsonProperty private int maxDuration;
    @JsonProperty private int maxBitrate;
    @JsonProperty private String resolution;
    @JsonProperty private String status;
    @JsonProperty private String hlsStatus;
    @JsonProperty private String multiBroadcastTag;
    @JsonProperty private boolean hasAudio = true;
    @JsonProperty private boolean hasVideo = true;
    @JsonProperty private StreamMode streamMode = StreamMode.AUTO;
    private List<Rtmp> rtmpList = new ArrayList<>(5);
    private String hls;

    /**
     * Do not call the <code>Broadcast()</code> constructor. To start a live streaming broadcast,
     * call the {@link OpenTok#startBroadcast OpenTok.startBroadcast()} method.
     */
    protected Broadcast() {
    }

    /**
     * Do not call the <code>makeBroadcast()</code> method. To start a live streaming broadcast,
     * call the {@link OpenTok#startBroadcast OpenTok.startBroadcast()} method.
     */
    @JsonCreator
    public static Broadcast makeBroadcast() {
        return new Broadcast();
    }

    /**
     * The broadcast ID.
     */
    public String getId() {
        return id;
    }

    /**
     * The session ID of the OpenTok session associated with this broadcast.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * The OpenTok API key associated with the broadcast.
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * The time at which the broadcast was started, in milliseconds since the Unix epoch.
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * The time at which the broadcast was updated, in milliseconds since the Unix epoch.
     */
    public long getUpdatedAt() {
        return updatedAt;
    }

    /**
     * The maximum duration of the broadcast in seconds.
     *
     * @return The maximum duration.
     */
    public int getMaxDuration() {
        return maxDuration;
    }

    /**
     * Maximum bitrate (bits per second) is an optional value allowed for the broadcast composing.
     *
     * @return The maximum bitrate.
     */
    public int getMaxBitrate() {
        return maxBitrate;
    }

    /**
     * The broadcast resolution.
     */
    public String getResolution() {
        return resolution;
    }

    /**
     * The broadcast status, either "started" or "stopped".
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the multiBroadcastTag if set for the Broadcast.
     */
    public String getMultiBroadcastTag() {
        return multiBroadcastTag;
    }

    /**
     * Details on the HLS and RTMP broadcast streams. For an HLS stream, the URL is provided.
     * See the <a href="https://tokbox.com/developer/guides/broadcast/live-streaming/">OpenTok
     * live streaming broadcast developer guide</a> for more information on how to use this URL.
     * For each RTMP stream, the RTMP server URL and stream name are provided, along with the RTMP
     * stream's status.
     */
    @JsonProperty("broadcastUrls")
    @SuppressWarnings("unchecked")
    private void unpack(Map<String,Object> broadcastUrls) {
        if (broadcastUrls == null) return;
        hls = (String) broadcastUrls.get("hls");
        hlsStatus = (String) broadcastUrls.get("hlsStatus");
        Iterable<Map<String,String>> rtmpResponse = (Iterable<Map<String,String>>)broadcastUrls.get("rtmp");
        if (rtmpResponse == null) return;
        for (Map<String,String> element : rtmpResponse) {
            Rtmp rtmp = new Rtmp();
            rtmp.setId(element.get("id"));
            rtmp.setServerUrl(element.get("serverUrl"));
            rtmp.setStreamName(element.get("streamName"));
            rtmp.setStatus(element.get("status"));
            this.rtmpList.add(rtmp);
        }
    }

    /**
     * The HLS URL (if there is one) of the broadcast.
     */
    public String getHls() {
        return hls;
    }

    /**
     * The HLS status of the broadcast if known. One of: "connecting", "ready", "live", "ended", "error".
     *
     * @return The HLS status as a string (if applicable).
     */
    public String getHlsStatus() {
        return hlsStatus;
    }

    /**
    * A list of RTMP URLs (if there are any) of the broadcast.
     */
    public List<Rtmp> getRtmpList() {
        return rtmpList;
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
     * The stream mode to used for selecting streams to be included in this archive:
     * <code>StreamMode.AUTO</code> or <code>StreamMode.MANUAL</code>.
     */
    public StreamMode getStreamMode() {
        return streamMode;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
