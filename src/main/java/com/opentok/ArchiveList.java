/**
 * OpenTok Java SDK
 * Copyright (C) 2015 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an list of archives of OpenTok session(s).
 */
@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public class ArchiveList extends ArrayList<Archive> {

    private int totalCount;

    /**
     * The total number of Archives for the API Key.
     */
    public int getTotalCount() {
        return totalCount;
    }

    private void setItems(List<Archive> archives) {
        this.clear();
        this.addAll(archives);
    }

    private void setCount(int count) {
        this.totalCount = count;
    }
}
