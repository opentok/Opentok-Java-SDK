/**
 * OpenTok Java SDK
 * Copyright (C) 2018 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an archive of an OpenTok session.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Broadcast {

    @JsonProperty private String id;
    @JsonProperty private String sessionId;
    @JsonProperty private int projectId;
    @JsonProperty private long createdAt;
    @JsonProperty private long updatedAt;
    @JsonProperty private String resolution;
    @JsonProperty private String status;
    private List<Rtmp> rtmpList = new ArrayList<>();
    private String hls;

    protected Broadcast() {
    }

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
     * The session ID of the OpenTok session associated with this archive.
     */
    public String getSessionId() {
        return sessionId;
    }


    /**
     * The OpenTok API key associated with the archive.
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * The time at which the archive was created, in milliseconds since the Unix epoch.
     */
    public long getCreatedAt() {
        return createdAt;
    }
    /**
     * The time at which the archive was created, in milliseconds since the Unix epoch.
     */
    public long getUpdatedAt() {
        return updatedAt;
    }
    /**
     * The name of the archive.
     */
    public String getResolution() {
        return resolution;
    }

    /**
     * The name of the archive.
     */
    public String getStatus() {
        return status;
    }
    /**
     * The name of the archive.
     *
     */
    @JsonProperty("broadcastUrls")
    private void unpack(Map<String,Object> broadcastUrls) {
        if (broadcastUrls == null) return;
        hls = (String)broadcastUrls.get("hls");
        ArrayList<Map<String,String>> rtmpResponse = (ArrayList<Map<String,String>>)broadcastUrls.get("rtmp");
        if (rtmpResponse == null || rtmpResponse.size() == 0) return;
        for ( Map<String,String> element : rtmpResponse) {
            Rtmp rtmp = new Rtmp();
            rtmp.setId(element.get("id"));
            rtmp.setServerUrl(element.get("serverUrl"));
            rtmp.setStreamName(element.get("streamName"));
            this.rtmpList.add(rtmp);
        }
    }
    /**
     * The name of the archive if present else null.
     */
    public String getHls() {
        return hls;
    }
    /**
     * The name of the archive.
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

}
