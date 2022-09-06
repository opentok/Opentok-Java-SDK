/**
 * OpenTok Java SDK
 * Copyright (C) 2022 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

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
		private String name;

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

	private String url;
	private int maxDuration;
	private String resolution;
	private String statusCallbackUrl;
	private Properties properties;

	private RenderProperties(Builder builder) {
		this.url = Objects.requireNonNull(builder.url, "URL is required");
		this.resolution = builder.resolution;
		this.maxDuration = builder.maxDuration;
		this.properties = builder.properties;
		this.statusCallbackUrl = builder.statusCallbackUrl;
	}

	public static class Builder {
		private String url;
		private int maxDuration = 7200;
		private String resolution = "1280x720";
		private String statusCallbackUrl;
		private Properties properties;

		public Builder url(String url) {
			this.url = url;
			return this;
		}

		public Builder maxDuration(int maxDuration) {
			this.maxDuration = maxDuration;
			return this;
		}

		public Builder resolution(String resolution) {
			this.resolution = resolution;
			return this;
		}

		public Builder statusCallbackUrl(String statusCallbackUrl) {
			this.statusCallbackUrl = statusCallbackUrl;
			return this;
		}

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

	public String url() {
		return url;
	}

	public int maxDuration() {
		return maxDuration;
	}

	public String resolution() {
		return resolution;
	}

	public String statusCallbackUrl() {
		return statusCallbackUrl;
	}

	public Properties properties() {
		return properties;
	}
}
