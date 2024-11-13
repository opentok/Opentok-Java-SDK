/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

/**
 * Represents HLS options for a live streaming broadcast. Pass this object
 * into the {@link BroadcastProperties.Builder#hls(Hls)} method. It is returned by the
 * {@link BroadcastProperties#hls()} method.
 */
public class Hls {
	private final boolean dvr;
	private final boolean lowLatency;

	/**
	 * Used to create the Hls object.
	 */
	public static class Builder {
		private boolean dvr = false;
		private boolean lowLatency = false;

		/**
		 * Whether to enable
		 * <a href="https://tokbox.com/developer/guides/broadcast/live-streaming/#dvr">DVR functionality</a> —
		 * rewinding, pausing, and resuming — in players that support it (true), or not (false, the default).
		 * With DVR enabled, the HLS URL will include a ?DVR query string appended to the end.
		 */
		public Builder dvr(boolean dvr) {
			this.dvr = dvr;
			return this;
		}

		/**
		 * Whether to enable
		 * <a href="https://tokbox.com/developer/guides/broadcast/live-streaming/#low-latency">low-latency mode</a>
		 * for the HLSstream. Some HLS players do not support low-latency mode. This feature is incompatible
		 * with DVR mode HLS broadcasts.
		 */
		public Builder lowLatency(boolean lowLatency) {
			this.lowLatency = lowLatency;
			return this;
		}

		/**
		 * Builds the HLS object with the selected settings.
		 */
		public Hls build() {
			return new Hls(this);
		}
	}

	protected Hls(Builder builder) {
		// Non-short-circuiting for setter
		if ((this.dvr = builder.dvr) & (this.lowLatency = builder.lowLatency)) {
			throw new IllegalArgumentException("Cannot set both dvr and lowLatency on HLS");
		}
	}

	/**
	 * Whether
	 * <a href="https://tokbox.com/developer/guides/broadcast/live-streaming/#dvr">DVR functionality</a> —
	 * rewinding, pausing, and resuming — is enabled in players that support it.
	 */
	public boolean dvr() {
		return dvr;
	}

	/**
	 * Whether
	 * <a href="https://tokbox.com/developer/guides/broadcast/live-streaming/#low-latency">low-latency mode</a>
	 * is enabled for the HLSstream. Some HLS players do not support low-latency mode.
	 */
	public boolean lowLatency() {
		return lowLatency;
	}
}
