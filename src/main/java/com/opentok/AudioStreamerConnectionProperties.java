/**
 * OpenTok Java SDK
 * Copyright (C) 2022 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.*;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#connectAudioStream(String, String, AudioStreamerConnectionProperties)} method.
 */
public class AudioStreamerConnectionProperties {
	private final URI uri;
	private final Collection<String> streams;
	private final Map<String, String> headers;

	/**
	 * Path of the WebSocket service endpoint.
	 * 
	 * @return A valid, non-null URI.
	 */
	public URI uri() {
		return uri;
	}

	/**
	 * Stream IDs to include in the request body.
	 *
	 * @return An immutable collection of stream IDs, if present.
	 */
	public Collection<String> streams() {
		return streams;
	}

	/**
	 * Additional headers to include in the request body.
	 * NOTE: The maximum length is 512 bytes.
	 *
	 * @return An immutable map of additional properties, if present.
	 */
	public Map<String, String> headers() {
		return headers;
	}

	/**
	 * Returns the name of the JSON object for the connection request body.
	 *
	 * @return "websocket".
	 */
	public String type() {
		return "websocket";
	}

	protected AudioStreamerConnectionProperties(Builder builder) {
		this.uri = Objects.requireNonNull(builder.uri);
		this.streams = builder.streams.isEmpty() ? null : Collections.unmodifiableCollection(builder.streams);
		this.headers = builder.headers.isEmpty() ? null : Collections.unmodifiableMap(builder.headers);
	}

	/**
	 * Intermediary stateful object used to construct {@link AudioStreamerConnectionProperties}.
	 */
	public static class Builder {
		private URI uri;
		private Collection<String> streams = new ArrayList<>();
		private Map<String, String> headers = new HashMap<>();

		/**
		 * Call this method and pass in the uri where your service is listening.
		 * This parameter is mandatory.
		 *
		 * @param uri A valid URI.
		 *
		 * @return The ConnectProperties.Builder object with the uri setting.
		 */
		public Builder uri(URI uri) {
			this.uri = uri;
			return this;
		}

		/**
		 * Call this method and pass in the uri where your service is listening.
		 *
		 * @param uri A valid string representation of a URI.
		 *
		 * @return The ConnectProperties.Builder object with the uri setting.
		 */
		public Builder uri(String uri) {
			return uri(URI.create(uri));
		}

		/**
		 * Adds an OpenTok stream (with the corresponding stream ID) to the include in the WebSocket audio.
		 *
		 * @param stream The Stream ID to add.
		 *
		 * @return This builder with the additional stream ID.
		 */
		public Builder addStream(String stream) {
			if (StringUtils.isBlank(stream)) {
				throw new IllegalArgumentException("Stream ID cannot be blank");
			}
			streams.add(stream);
			return this;
		}

		/**
		 * Adds the OpenTok streams (with the corresponding stream IDs) to the include in the WebSocket audio.
		 *
		 * @param streams The stream IDs to add.
		 *
		 * @return This builder with the additional stream IDs.
		 */
		public Builder addStreams(String... streams) {
			return addStreams(Arrays.asList(streams));
		}

		/**
		 * Adds the OpenTok streams (with the corresponding stream IDs) to the include in the WebSocket audio.
		 *
		 * @param streams The collection of stream IDs to add.
		 *
		 * @return This builder with the additional stream IDs.
		 */
		public Builder addStreams(Collection<String> streams) {
			this.streams.addAll(Objects.requireNonNull(streams));
			return this;
		}

		/**
		 * Puts all entries of the map into the headers parameter.
		 *
		 * @param headers The map of header key-value pairs to append.
		 *
		 * @return This builder with the specified headers included.
		 */
		public Builder addHeaders(Map<String, String> headers) {
			this.headers.putAll(Objects.requireNonNull(headers));
			return this;
		}

		/**
		 * Adds a header entry to this object's headers property.
		 *
		 * @param key Header key.
		 * @param value Header value.
		 *
		 * @return This builder with the additional header property.
		 */
		public Builder addHeader(String key, String value) {
			if (StringUtils.isBlank(key)) {
				throw new IllegalArgumentException("Property key cannot be blank");
			}
			headers.put(key, value);
			return this;
		}

		/**
		 * Builds the ConnectProperties object.
		 *
		 * @return The constructed {@link AudioStreamerConnectionProperties} object.
		 */
		public AudioStreamerConnectionProperties build() {
			return new AudioStreamerConnectionProperties(this);
		}
	}

}
