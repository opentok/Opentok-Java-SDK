/**
 * OpenTok Java SDK
 * Copyright (C) 2019 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of OpenTok Streams.
 */
@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public class StreamList extends ArrayList<Stream> {

    private int totalCount;

    /**
     * The total number of Streams in the StreamList.
     */
    public int getTotalCount() {
        return totalCount;
    }

    private void setItems(List<Stream> streams) {
        this.clear();
        this.addAll(streams);
    }

    private void setCount(int count) {
        this.totalCount = count;
    }
}
