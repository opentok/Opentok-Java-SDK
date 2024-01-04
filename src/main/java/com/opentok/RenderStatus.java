/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
 * http://www.tokbox.com
 * <p>
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the status of a {@link Render}.
 */
public enum RenderStatus {

	/**
	 * The Vonage Video API platform is in the process of connecting to the remote application at the URL provided.
	 * This is the initial state.
	 */
	STARTING,

	/**
	 * The Vonage Video API platform has successfully connected to the remote application server, and is
	 * publishing the web view to an OpenTok stream.
	 */
	STARTED,

	/**
	 * The Experience Composer has stopped.
	 */
	STOPPED,

	/**
	 * An error occurred and the Experience Composer could not proceed. It may occur at startup if the OpenTok server
	 * cannot connect to the remote application server or republish the stream. It may also occur at any point during
	 * the process due to an error in the Vonage Video API platform.
	 */
	FAILED;

	@JsonCreator
	public static RenderStatus fromString(String status) {
		try {
			return RenderStatus.valueOf(status.toUpperCase());
		}
		catch (NullPointerException | IllegalArgumentException e) {
			return null;
		}
	}

	@JsonValue
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
