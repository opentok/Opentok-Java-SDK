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
    
//    @JsonProperty private List broadcastUrls;
//
//    @JsonProperty private String hls;
//    @JsonProperty private String rtmp;



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
    @SuppressWarnings("unchecked")
    @JsonProperty("broadcastUrls")
    private void unpack(Map<String,Object> broadcastUrls) {
        hls = (String)broadcastUrls.get("hls");
        Map<String,Object> rtmp = (Map<String,Object>)broadcastUrls.get("rtmp");
        for (Map.Entry<String,Object> entry : rtmp.entrySet()) {
            Rtmp rtmpObject = new Rtmp();
            rtmpObject.setId(entry.getKey());
            Map<String,String> rtmpData = (Map<String,String>)entry.getValue();
            rtmpObject.setServerUrl(rtmpData.get("serverUrl"));
            rtmpObject.setStreamName(rtmpData.get("streamName"));
            this.rtmpList.add(rtmpObject);
        }
    }
//    public List getBroadcastUrls() {
//        return broadcastUrls;
//    }

//    /**
//     * The name of the archive.
//     */
//    public String getRtmp() {
//        return rtmp;
//    }
    /**
     * The name of the archive.
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
