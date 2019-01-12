/**
 * OpenTok Java SDK
 * Copyright (C) 2019 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Represents a stream in an OpenTok session.
 */
@JsonIgnoreProperties(ignoreUnknown=true)

public class Stream {
    @JsonProperty private String id;
    @JsonProperty private String videoType;
    @JsonProperty private String name;
    @JsonProperty private List<String> layoutClassList;

    protected Stream() {
    }

    @JsonCreator
    public static Stream makeStream() {
        return new Stream();
    }


    /**
     * The stream ID.
     */
    public String getId() {
        return id;
    }

    /**
     * The stream videoType.
     */
    public String getVideoType() {
        return videoType;
    }

    /**
     * The name of the stream.
     */
    public String getName() {
        return name;
    }

    /**
     * The layout class list of the stream.
     */
    public List<String> getLayoutClassList() {
        return layoutClassList;
    }

}
