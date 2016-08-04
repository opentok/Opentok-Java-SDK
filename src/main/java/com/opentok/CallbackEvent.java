package com.opentok;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CallbackEvent {
    /**
     * The session is not archived automatically. To archive the session, you can call the
     * OpenTok.StartArchive() method.
     */
    @JsonProperty("created")
    CREATED,

    /**
     * The session is archived automatically (as soon as there are clients publishing streams
     * to the session).
     */
    @JsonProperty("destroyed")
    DESTROYED;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
