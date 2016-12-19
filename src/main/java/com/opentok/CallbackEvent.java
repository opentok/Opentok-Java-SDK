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
 * Defines values for the event attribute of the
 * {@link Callback} class.
 */
public enum CallbackEvent {
    /**
     * This event is triggered for streams and connections when they are created.
     */
    @JsonProperty("created")
    CREATED,

    /**
     * This event is triggered for streams and connections when they are destroyed.
     */
    @JsonProperty("destroyed")
    DESTROYED,

    /**
     * This event is only triggered for archives when the archive state changes.
     */
    @JsonProperty("status")
    STATUS;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
