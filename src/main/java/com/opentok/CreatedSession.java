/**
 * OpenTok Java SDK
 * Copyright (C) 2025 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Used internally.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatedSession {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("application_id")
    private String applicationId;

    @JsonProperty("partner_id")
    private String partnerId;

    @JsonProperty("create_dt")
    private String createDt;

    @JsonProperty("media_server_url")
    private String mediaServerURL;

    protected CreatedSession() {
    }

    /**
     * Used internally. Use the {@link OpenTok#createSession(SessionProperties properties)}
     * method to create an OpenTok session.
     */
    @JsonCreator
    public static CreatedSession makeSession() {
        return new CreatedSession();
    }

    public String getId() {
        return sessionId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public String getCreateDt() {
        return createDt;
    }

    public String getMediaServerURL() {
        return mediaServerURL;
    }

    @Override
    public String toString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (Exception e) {
            return "";
        }

    }
}
