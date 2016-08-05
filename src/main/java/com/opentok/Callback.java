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
* Represents a callback registered for an OpenTok Cloud API event.
 *
 * @see OpenTok#registerCallback()
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
     * The time at which the callback was created, in milliseconds since the Unix epoch.
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
     *  The group of events this callback is registered for.
     */
    public CallbackGroup getGroup() {
        return group;
    }

    /**
     * The event this callback is registered for.
     */
    public CallbackEvent getEvent() {
        return event;
    }

    /**
     * The registered callback URL.
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