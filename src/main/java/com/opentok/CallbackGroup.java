/**
 * OpenTok Java SDK
 * Copyright (C) 2016 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Defines values for the group attribute of the
 * {@link Callback} class.
 */
public enum CallbackGroup {

    /**
     * This group represents all the events related to connections (right now created and destroyed events).
     */
    @JsonProperty("connection")
    CONNECTION,

    /**
     * This group represents all the events related to streams (right now created and destroyed events).
     */
    @JsonProperty("stream")
    STREAM,

    /**
     * This group represents all the events related to archives (right now status events).
     */
    @JsonProperty("archive")
    ARCHIVE;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
