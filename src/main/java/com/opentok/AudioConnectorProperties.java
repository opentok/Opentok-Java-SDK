/**
 * OpenTok Java SDK
 * Copyright (C) 2025 Vonage.
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
 * {@link OpenTok#connectAudioStream(String, String, AudioConnectorProperties)} method.
 */
public class AudioConnectorProperties {
	private final URI uri;
	private final Collection<String> streams;
	private final Map<String, String> headers;

	/**
	 * The WebSocket URI to be used for the destination of the audio stream.
	 *
	 * @return A valid, non-null URI.
	 */
	public URI uri() {
		return uri;
	}

	/**
	 * (OPTIONAL)
	 * A collection of stream IDs for the OpenTok streams included in the WebSocket audio.
	 * If this collection is empty, all streams in the session will be included.
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

	protected AudioConnectorProperties(Builder builder) {
		this.uri = Objects.requireNonNull(builder.uri);
		this.streams = builder.streams.isEmpty() ? null : Collections.unmodifiableCollection(builder.streams);
		this.headers = builder.headers.isEmpty() ? null : Collections.unmodifiableMap(builder.headers);
	}

	/**
	 * Intermediary stateful object used to construct {@link AudioConnectorProperties}.
	 */
	public static class Builder {
		private final URI uri;
		private final Collection<String> streams = new ArrayList<>();
		private final Map<String, String> headers = new HashMap<>();

		/**
		 * Constructor for the AudioConnectorProperties.Builder, using a URI to
		 * define the WebSocket URI.
		 *
		 * @param uri The publicly reachable WebSocket URI to be used for the destination
		 * of the audio stream.
		 */
		public Builder(URI uri) {
			this.uri = uri;
		}

		/**
		 * Constructor for the AudioConnectorProperties.Builder, using a string to
		 * define the WebSocket URI.
		 *
		 * @param uri The publicly reachable WebSocket URI to be used for the destination of
		 * the audio stream, as a string (such as "wss://example.com/ws-endpoint").
		 */
		public Builder(String uri) {
			this(URI.create(uri));
		}

		/**
		 * Adds an OpenTok stream (with the corresponding stream ID) to include in the WebSocket audio.
		 * If the AudioConnectorProperties includes no streams, all streams in the session
		 * will be included.
		 *
		 * @param stream The stream ID.
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
		 * Adds OpenTok streams (with the corresponding stream IDs) to include in the WebSocket audio.
		 * If the AudioConnectorProperties includes no streams, all streams in the session
		 * will be included.
		 *
		 * @param streams The stream IDs to add.
		 *
		 * @return This builder with the additional stream IDs.
		 */
		public Builder addStreams(String... streams) {
			return addStreams(Arrays.asList(streams));
		}

		/**
		 * Adds OpenTok streams (with the corresponding stream IDs) to include in the WebSocket audio.
		 * If the AudioConnectorProperties includes no streams, all streams in the session
		 * will be included.
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
		 * Puts all entries of the map into the headers parameter. The headers will
		 * be sent to your WebSocket server with each message.
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
		 * Adds a header entry to this object's headers property. The header will
		 * be sent to your WebSocket server with each message.
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
		 * Builds the AudioConnectorProperties object.
		 *
		 * @return The constructed {@link AudioConnectorProperties} object.
		 */
		public AudioConnectorProperties build() {
			return new AudioConnectorProperties(this);
		}
	}

}
