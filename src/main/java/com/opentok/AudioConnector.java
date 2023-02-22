/**
 * OpenTok Java SDK
 * Copyright (C) 2023 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Audio Connector instance.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AudioConnector {
	@JsonProperty private String id;
	@JsonProperty private String connectionId;

	protected AudioConnector() {}

	/**
	 * Do not call this method. To initiate an audio stream connection to a session, call the
	 * {@link OpenTok#connectAudioStream(String, String, AudioConnectorProperties)} method.
	 */
	@JsonCreator
	public static AudioConnector makeConnection() {
		return new AudioConnector();
	}

	/**
	 * @return A unique ID identifying the Audio Connector WebSocket connection.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return The OpenTok connection ID for the Audio Connector WebSocket connection in the OpenTok session.
	 */
	public String getConnectionId() {
		return connectionId;
	}
}
