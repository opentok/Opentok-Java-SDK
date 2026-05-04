/**
 * OpenTok Java SDK
 * Copyright (C) 2026 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of connections in an OpenTok session.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class ConnectionList extends ArrayList<Connection> {

    private int count;
    private String projectId;
    private String sessionId;

    /**
     * The total number of connections in the session.
     */
    public int getCount() {
        return count;
    }

    /**
     * The API key (project ID) for the session.
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * The session ID.
     */
    public String getSessionId() {
        return sessionId;
    }

    private void setCount(int count) {
        this.count = count;
    }

    private void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    private void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    private void setItems(List<Connection> connections) {
        this.clear();
        this.addAll(connections);
    }
}
