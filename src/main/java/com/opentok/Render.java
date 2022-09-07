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
 * Represents an Experience Composer element response.
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

	/**
	 * The Render ID.
	 */
	public String getId() {
		return id;
	}

	/**
	 * The Session ID.
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * The Project ID.
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * The time the Experience Composer started, expressed in milliseconds since the Unix epoch.
	 */
	public long getCreatedAt() {
		return createdAt;
	}

	/**
	 * This timestamp matches the createdAt timestamp when calling {@link OpenTok#getRender(String)}.
	 */
	public long getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * A publicly reachable URL controlled by the customer and capable of generating the content to be rendered
	 * without user intervention.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * The resolution of the Experience Composer (either "640x480", "1280x720", "480x640", or "720x1280").
	 */
	public String getResolution() {
		return resolution;
	}

	/**
	 * The status of the Experience Composer.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * The ID of the composed stream being published. The streamId is not available when the status is "starting"
	 * and may not be available when the status is "failed".
	 */
	public String getStreamId() {
		return streamId;
	}

	/**
	 * The reason field is only available when the status is either "stopped" or "failed". If the status is stopped,
	 * the reason field will contain either "Max Duration Exceeded" or "Stop Requested." If the status is failed, the
	 * reason will contain a more specific error message.
	 */
	public String getReason() {
		return reason;
	}
}
