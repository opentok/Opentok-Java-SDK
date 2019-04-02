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
 * Represents a list of broadcasts of OpenTok session(s).
 */
@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public class BroadcastList extends ArrayList<Broadcast> {

    private int totalCount;

    /**
     * The total number of Broadcasts for the API Key.
     */
    public int getTotalCount() {
        return totalCount;
    }

    private void setItems(List<Broadcast> broadcasts) {
        this.clear();
        this.addAll(broadcasts);
    }

    private void setCount(int count) {
        this.totalCount = count;
    }
}
