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

/**
 * Represents a stream  of an OpenTok session.
 */
@JsonIgnoreProperties(ignoreUnknown=true)

public class Sip {
    @JsonProperty private String id;
    @JsonProperty private String connectionId;
    @JsonProperty private String streamId;


    protected Sip() {
    }

    @JsonCreator
    public static Sip makeSip() {
        return new Sip();
    }


    /**
     * The stream ID.
     */
    public String getId() {
        return id;
    }
    /**
     * The connection ID.
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * The stream ID.
     */
    public String getStreamId() {
        return streamId;
    }
}

