/**
 * OpenTok Java SDK
 * Copyright (C) 2022 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Audio Stream connection response.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Connect {

	@JsonProperty private String id;
	@JsonProperty private String connectionId;

	protected Connect() {}

	/**
	 * Do not call this method. To initiate a connection, call the
	 * {@link OpenTok#connectAudioStream(String, String, ConnectProperties)} method.
	 */
	@JsonCreator
	public static Connect makeConnect() {
		return new Connect();
	}

	/**
	 * A unique call ID identifying the Audio Streamer WebSocket connection.
	 */
	public String getId() {
		return id;
	}

	/**
	 * The OpenTok connection ID for the Audio Streamer WebSocket connection in the OpenTok session.
	 */
	public String getConnectionId() {
		return connectionId;
	}
}
