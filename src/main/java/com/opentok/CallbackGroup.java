/**
 * OpenTok Java SDK
 * Copyright (C) 2016 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines values for the archiveMode parameter of the
 * {@link SessionProperties.Builder#archiveMode(CallbackGroup archiveMode)} method.
 */
public enum CallbackGroup {

    /**
     * The session is not archived automatically. To archive the session, you can call the
     * OpenTok.StartArchive() method.
     */
    @JsonProperty("connection")
    CONNECTION,

    /**
     * The session is archived automatically (as soon as there are clients publishing streams
     * to the session).
     */
    @JsonProperty("stream")
    STREAM;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
