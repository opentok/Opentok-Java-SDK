/**
 * OpenTok Java SDK
 * Copyright (C) 2018 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;


import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#setArchiveStreamsLayout(String, StreamListProperties)}
 *
 * @see OpenTok#setArchiveStreamsLayout(String, StreamListProperties)
 */
public class StreamProperties {
    private String id = null;
    private List<String> layoutClassList;

    private StreamProperties(StreamProperties.Builder builder) {
        this.id = builder.id;
        this.layoutClassList = builder.layoutClassList;
    }
    /**
     * Use this class to create a StreamProperties object.
     *
     * @see StreamProperties
     */
    public static class Builder {
        private String id = null;
        private List<String> layoutClassList = new ArrayList<>();

        /**
         * Call this method to set the id of the stream.
         *
         * @param id The stream id
         *
         * @return The StreamProperties.Builder object with the id setting.
         */
        public StreamProperties.Builder id(String id) {
            this.id = id;
            return this;
        }
        /**
         * Call this method to set the individual layout classes of the stream.
         *
         * @param layoutClass  The layout class string for  layoutClassList. We do not check
         *                     for null/empty/duplicate strings.
         *
         * @return The StreamListProperties.Builder object.
         */
        public StreamProperties.Builder addLayoutClass(String layoutClass) {
            this.layoutClassList.add(String.format("\"%s\"",layoutClass));
            return this;
        }

        /**
         * Builds the StreamProperties object.
         *
         * @return The StreamProperties object.
         */
        public StreamProperties build() {
            return new StreamProperties(this);
        }
    }
    /**
     * Returns the id  of the stream
     */
    public String id() {
        return id;
    }


    /**
     *   Returns an array of layout classes (each strings) for the stream.
     *   An example: ["full", "focus"]
     */
    public String getLayoutClassString() {
        return String.format("[%s]", StringUtils.join(layoutClassList,","));
    }

};


