/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
 * http://www.tokbox.com
 * <p>
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonValue;
import java.net.URI;
import java.util.Objects;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#startRender(String sessionId, String token, RenderProperties properties)} method.
 */
public class RenderProperties {

	/**
	 * Represents the <code>properties</code> parameter of {@linkplain RenderProperties}.
	 */
	public static class Properties {
		private final String name;

		public Properties(String name) {
			if ((this.name = name) == null || name.isEmpty()) {
				throw new IllegalArgumentException("Name is required.");
			}
			if (name.length() > 200) {
				throw new IllegalArgumentException("Name '"+name+"' exceeds 200 characters.");
			}
		}

		public String name() {
			return name;
		}
	}

	/**
	 * Represents the <code>resolution</code> parameter of {@linkplain RenderProperties}.
	 */
	public enum Resolution {
		/**
		 * 480x640
		 */
		SD_VERTICAL("480x640"),
		/**
		 * 640x480
		 */
		SD_HORIZONTAL("640x480"),
		/**
		 * 720x1280
		 */
		HD_VERTICAL("720x1280"),
		/**
		 * 1280x720
		 */
		HD_HORIZONTAL("1280x720");

		private final String value;

		Resolution(String value) {
			this.value = value;
		}

		@JsonValue
		public String toString() {
			return value;
		}
	}

	private final URI url;
	private final int maxDuration;
	private final Resolution resolution;
	private final Properties properties;

	private RenderProperties(Builder builder) {
		this.url = Objects.requireNonNull(builder.url, "URL is required");
		this.resolution = builder.resolution;
		this.maxDuration = builder.maxDuration;
		this.properties = builder.properties;
	}

	/**
	 * Entry point for constructing an instance of {@linkplain RenderProperties}.
	 *
	 * @return A new Builder instance.
	 */
	public static Builder Builder() {
		return new Builder();
	}

	/**
	 * Builder for defining the parameters of {@link RenderProperties}.
	 */
	public static class Builder {
		private URI url;
		private int maxDuration = 7200;
		private Resolution resolution = Resolution.HD_HORIZONTAL;
		private Properties properties;

		/**
		 * URL of the customer service where the callbacks will be received.
		 *
		 * @param url The URL as a String.
		 * @return This Builder.
		 */
		public Builder url(String url) {
			return url(URI.create(url));
		}

		/**
		 * URL of the customer service where the callbacks will be received.
		 *
		 * @param url The URL as a URI.
		 * @return This Builder.
		 */
		public Builder url(URI url) {
			this.url = url;
			return this;
		}

		public Builder maxDuration(int maxDuration) {
			this.maxDuration = maxDuration;
			return this;
		}

		/**
		 * Resolution of the display area for the composition.
		 *
		 * @param resolution The resolution, as an enum.
		 * @return This Builder.
		 */
		public Builder resolution(Resolution resolution) {
			this.resolution = resolution;
			return this;
		}

		/**
		 * Initial configuration of Publisher properties for the composed output stream.
		 *
		 * @param properties The properties value.
		 * @return This Builder.
		 */
		public Builder properties(Properties properties) {
			this.properties = properties;
			return this;
		}

		/**
		 * Builds the RenderProperties object.
		 *
		 * @return The RenderProperties object.
		 */
		public RenderProperties build() {
			return new RenderProperties(this);
		}
	}

	public URI url() {
		return url;
	}

	public int maxDuration() {
		return maxDuration;
	}

	public Resolution resolution() {
		return resolution;
	}

	public Properties properties() {
		return properties;
	}
}
