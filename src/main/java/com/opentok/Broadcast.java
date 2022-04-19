/**
 * OpenTok Java SDK
 * Copyright (C) 2022 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
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
         * Strams will be included in the archive based on calls to the
         * {@link OpenTok#addBroadcastStream(String, String, boolean, boolean)} and
         * {@link OpenTok#removeBroadcastStream(String, String)} methods.
         */
        MANUAL;

        @JsonValue
        public String toString() { return super.toString().toLowerCase(); }
    }

    @JsonProperty private String id;
    @JsonProperty private String sessionId;
    @JsonProperty private int projectId;
    @JsonProperty private long createdAt;
    @JsonProperty private long updatedAt;
    @JsonProperty private String resolution;
    @JsonProperty private String status;
    @JsonProperty private StreamMode streamMode = StreamMode.AUTO;
    private List<Rtmp> rtmpList = new ArrayList<>();
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
        Iterable<Map<String,String>> rtmpResponse = (Iterable<Map<String,String>>)broadcastUrls.get("rtmp");
        if (rtmpResponse == null) return;
        for (Map<String,String> element : rtmpResponse) {
            Rtmp rtmp = new Rtmp();
            rtmp.setId(element.get("id"));
            rtmp.setServerUrl(element.get("serverUrl"));
            rtmp.setStreamName(element.get("streamName"));
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
    * A list of RTMP URLs (if there are any) of the broadcast.
     */
    public List<Rtmp> getRtmpList() {
        return rtmpList;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * The stream mode to used for selecting streams to be included in this archive:
     * <code>StreamMode.AUTO</code> or <code>StreamMode.MANUAL</code>.
     */
    public StreamMode getStreamMode() {
        return streamMode;
    }

}
