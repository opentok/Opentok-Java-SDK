/**
 * OpenTok Java SDK
 * Copyright (C) 2022 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Render element response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Render {
	@JsonProperty private String id;
	@JsonProperty private String sessionId;
	@JsonProperty private String projectId;
	@JsonProperty private long createdAt;
	@JsonProperty private long updatedAt;
	@JsonProperty private String url;
	@JsonProperty private String resolution;
	@JsonProperty private String status;
	@JsonProperty private String streamId;
	@JsonProperty private String reason;

	protected Render() {}

	public String getId() {
		return id;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getProjectId() {
		return projectId;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public long getUpdatedAt() {
		return updatedAt;
	}

	public String getUrl() {
		return url;
	}

	public String getResolution() {
		return resolution;
	}

	public String getStatus() {
		return status;
	}

	public String getStreamId() {
		return streamId;
	}

	public String getReason() {
		return reason;
	}
}
