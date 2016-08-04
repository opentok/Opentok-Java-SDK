/**
 * OpenTok Java SDK
 * Copyright (C) 2016 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
* Represents a callback of an OpenTok session.
*/
@JsonIgnoreProperties(ignoreUnknown=true)
public class Callback {

    @JsonProperty private long createdAt;
    @JsonProperty private String id;
    @JsonProperty private CallbackGroup group;
    @JsonProperty private CallbackEvent event;
    @JsonProperty private String url;

    protected Callback() {
    }

    @JsonCreator
    public static Callback makeCallback() {
        return new Callback();
    }

    /**
     * The time at which the archive was created, in milliseconds since the Unix epoch.
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * The callback ID.
     */
    public String getId() {
        return id;
    }

    /**
     * The name of the archive.
     */
    public CallbackGroup getGroup() {
        return group;
    }

    /**
     * The name of the archive.
     */
    public CallbackEvent getEvent() {
        return event;
    }

    /**
     * The download URL of the available MP4 file. This is only set for an archive with the status
     * set to Status.AVAILABLE; for other archives, (including archives with the status of
     * Status.UPLOADED) this method returns null. The download URL is obfuscated, and the file
     * is only available from the URL for 10 minutes. To generate a new URL, call the
     * {@link com.opentok.OpenTok#listArchives()} or {@link com.opentok.OpenTok#getArchive(String)}
     * method.
     */
    public String getUrl() {
        return url;
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