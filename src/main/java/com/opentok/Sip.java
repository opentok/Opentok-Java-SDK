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
 * An object representing an OpenTok SIP call.
 * <p>
 * Do not call the <code>new()</code> constructor. To start a SIP call, call the
 * {@link OpenTok#dial OpenTok.dial()} method.
 */
@JsonIgnoreProperties(ignoreUnknown=true)

public class Sip {
    @JsonProperty private String id;
    @JsonProperty private String connectionId;
    @JsonProperty private String streamId;

    protected Sip() {
    }

    /**
     * Used internally. Use the {@link OpenTok#dial OpenTok.dial()} method to
     * start a SIP call.
     */
    @JsonCreator
    public static Sip makeSip() {
        return new Sip();
    }

    /**
     * The unique ID of the SIP conference.
     */
    public String getId() {
        return id;
    }

    /**
     * The connection ID of the audio-only stream that is put into an OpenTok Session.
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * The stream ID of the audio-only stream that is put into an OpenTok Session.
     */
    public String getStreamId() {
        return streamId;
    }
}

