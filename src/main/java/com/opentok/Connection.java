/**
 * OpenTok Java SDK
 * Copyright (C) 2026 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.*;

/**
 * Represents a connection in an OpenTok session.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Connection {

    /**
     * The state of a connection in a session.
     */
    public enum ConnectionState {
        CONNECTING, CONNECTED, DISCONNECTED;

        @JsonCreator
        public static ConnectionState fromString(String state) {
            try {
                return ConnectionState.valueOf(state.toUpperCase());
            }
            catch (NullPointerException | IllegalArgumentException e) {
                return null;
            }
        }

        @JsonValue
        @Override
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    @JsonProperty private String connectionId;
    @JsonProperty private long createdAt;
    @JsonProperty private ConnectionState connectionState;

    protected Connection() {
    }

    @JsonCreator
    public static Connection makeConnection() {
        return new Connection();
    }

    /**
     * The connection ID.
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * The time at which the client connected, in milliseconds since the Unix epoch.
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * The current state of the connection.
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }
}
