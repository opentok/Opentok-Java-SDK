/**
 * OpenTok Java SDK
 * Copyright (C) 2022 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

/**
 * POJO for the 'hls' object node in the 'outputs' section of a Broadcast request's body.
 */
public class Hls {

	private final boolean dvr;
	private final boolean lowLatency;

	public static class Builder {
		private boolean dvr = false;
		private boolean lowLatency = false;

		public Builder dvr(boolean dvr) {
			this.dvr = dvr;
			return this;
		}

		public Builder lowLatency(boolean lowLatency) {
			this.lowLatency = lowLatency;
			return this;
		}

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

	public boolean dvr() {
		return dvr;
	}

	public boolean lowLatency() {
		return lowLatency;
	}
}
