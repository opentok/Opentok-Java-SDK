/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the response from {@link OpenTok#startCaptions(String, String, CaptionProperties)}.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Caption {
    private String captionsId;

    /**
     * The unique ID for the audio captioning session.
     *
     * @return The captions UUID as a string.
     */
    @JsonProperty("captionsId")
    public String getCaptionsId() {
        return captionsId;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
