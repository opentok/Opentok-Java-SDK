/**
 * OpenTok Java SDK
 * Copyright (C) 2025 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents an RTMP stream in an OpenTok session.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Rtmp {
    public String id;
    private String serverUrl;
    private String streamName;
    private String status;

    /**
     * The stream ID.
     */
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }

    /**
     * The RTMP server URL.
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * The stream name.
     */
    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }
    public String getStreamName() {
        return streamName;
    }

    /**
     * @return The RTMP status.
     */
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}

