/**
 * OpenTok Java SDK
 * Copyright (C) 2018 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

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


    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "";
        }

    }

}
