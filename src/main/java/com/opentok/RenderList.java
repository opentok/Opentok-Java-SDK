/**
 * OpenTok Java SDK
 * Copyright (C) 2022 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the response returned from calling the {@link OpenTok#listRenders()} method.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RenderList {
	@JsonProperty private int count;
	@JsonProperty private List<Render> items;

	protected RenderList() {}

	public int getCount() {
		return count;
	}

	public List<Render> getItems() {
		return items;
	}
}
