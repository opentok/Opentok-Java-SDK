/**
 * OpenTok Java SDK
 * Copyright (C) 2018 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a stream in an OpenTok session.
 */
@JsonIgnoreProperties(ignoreUnknown=true)

public class Rtmp {
    public String id;
    private String serverUrl;
    private String streamName;

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
     * The server URL.
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


}

