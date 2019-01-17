/**
 * OpenTok Java SDK
 * Copyright (C) 2019 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;


import java.util.ArrayList;
import java.util.List;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#setStreamLayouts(String, StreamListProperties)} method.
 *
 * @see OpenTok#setStreamLayouts(String, StreamListProperties)
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
         * Call this method to set the ID of the stream.
         *
         * @param id The stream ID.
         *
         * @return The StreamProperties.Builder object with the ID setting.
         */
        public StreamProperties.Builder id(String id) {
            this.id = id;
            return this;
        }
        /**
         * Call this method to set an individual layout class for the stream.
         * Call this method multiple times to set multiple classes for a stream.
         * If you do not call this method when building the StreamProperties object,
         * the stream's layout class list will be cleared.
         *
         * @param layoutClass  A layout class string for the stream. We do not check
         *                     for null, empty, or duplicate strings.
         *
         * @return The StreamListProperties.Builder object.
         */
        public StreamProperties.Builder addLayoutClass(String layoutClass) {
            this.layoutClassList.add(layoutClass);
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
     * Returns the ID of the stream.
     */
    public String id() {
        return id;
    }


    /**
     * Returns the list of layout classes for the stream.
     */
    public List<String>  getLayoutClassList() {
        return layoutClassList;
    }
};


