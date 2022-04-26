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
 * {@link OpenTok#connectAudioStream(String, String, ConnectProperties)} method.
 */
public class ConnectProperties {
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
	 *
	 * NOTE: The maximum length of this should be 512 bytes.
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

	protected ConnectProperties(Builder builder) {
		this.uri = Objects.requireNonNull(builder.uri);
		this.streams = builder.streams.isEmpty() ? null : Collections.unmodifiableCollection(builder.streams);
		this.headers = builder.headers.isEmpty() ? null : Collections.unmodifiableMap(builder.headers);
	}

	/**
	 * Intermediary stateful object used to construct {@link ConnectProperties}.
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
		 * Sets the collection of stream IDs to the parameter.
		 *
		 * @param streams The collection of stream IDs.
		 *
		 * @return This builder with the streams property set.
		 */
		public Builder streams(Collection<String> streams) {
			this.streams = Objects.requireNonNull(streams);
			return this;
		}

		/**
		 * Adds the stream ID to the streams property.
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
		 * Adds the stream IDs to the streams property.
		 *
		 * @param streams The stream IDs to add.
		 *
		 * @return This builder with the additional stream IDs.
		 */
		public Builder addStreams(String... streams) {
			return addStreams(Arrays.asList(streams));
		}

		/**
		 * Adds the stream IDs to the streams property.
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
		 * Sets the map of additional headers to the parameter.
		 *
		 * @param headers The map of header properties.
		 *
		 * @return This builder with the headers property set.
		 */
		public Builder headers(Map<String, String> headers) {
			this.headers = Objects.requireNonNull(headers);
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
		 * @return The constructed {@link ConnectProperties} object.
		 */
		public ConnectProperties build() {
			return new ConnectProperties(this);
		}
	}

}
