/**
 * OpenTok Java SDK
 * Copyright (C) 2018 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of rtmp properties of a broadcast session.
 */

public class RtmpList extends ArrayList<RtmpProperties> {

    private int totalCount;

    /**
     * The total number of properties for rtmp.
     */
    public int getTotalCount() {
        return totalCount;
    }

    private void setItems(List<RtmpProperties> rtmps) {
        this.clear();
        this.addAll(rtmps);
    }

    private void setCount(int count) {
        this.totalCount = count;
    }
}

