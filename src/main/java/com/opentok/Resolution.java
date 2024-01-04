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
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Defines valid video resolutions.
 */
public enum Resolution {
	/**
	 * 480p landscape (640x480)
	 */
	SD_HORIZONTAL("640x480"),

	/**
	 * 480p portrait (480x640)
	 */
	SD_VERTICAL("480x640"),

	/**
	 * 720p landscape (1280x720)
	 */
	HD_HORIZONTAL("1280x720"),

	/**
	 * 720p portrait (720x1280)
	 */
	HD_VERTICAL("720x1280"),

	/**
	 * 1080p landscape (1920x1080)
	 */
	FHD_HORIZONTAL("1920x1080"),

	/**
	 * 1080p portrait (1080x1920)
	 */
	FHD_VERTICAL("1080x1920");

	private static final Map<String, Resolution> RESOLUTION_INDEX =
			Arrays.stream(Resolution.values()).collect(Collectors.toMap(
					Resolution::toString, Function.identity()
			));

	private final String value;

	Resolution(String value) {
		this.value = value;
	}

	@JsonValue
	@Override
	public String toString() {
		return value;
	}

	@JsonCreator
	public static Resolution fromString(String resolution) {
		return RESOLUTION_INDEX.getOrDefault(resolution, null);
	}
}
