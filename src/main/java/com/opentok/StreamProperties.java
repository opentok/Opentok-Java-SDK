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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
         * @param layoutClass  The layout class string for  layoutClassList
         *
         * @return The StreamListProperties.Builder object.
         */
        public StreamProperties.Builder addLayoutClass(String layoutClass) {
            this.layoutClassList.add(layoutClass);
            return this;
        }

        /**
         * Call this method to set layoutClassList of the stream.
         *
         * @param layoutClassList The layoutClassList of the stream
         *
         * @return The StreamProperties.Builder object with the layoutClassList setting.
         */
//        public StreamProperties.Builder layoutClassList (List<String> layoutClassList) {
//            this.layoutClassList = layoutClassList;
//            return this;
//        }

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
     *   Returns the layoutClassList of the stream
     */
    public List<String> getLayoutClassList() {
        return layoutClassList;
    }

    /**
     *   Returns an array of layout classes (each strings) for the stream.
     *   An example: ["full", "focus"]
     */
    public String getLayoutClassString() {
        String listString = "[";
        for (String s : layoutClassList)
        {
            listString += String.format("\"%s\",",s);
           // listString += "\"" + s + "\",";
        }
        listString = StringUtils.substring(listString, 0, listString.length() > 1 ? listString.length() - 1 : 0);
        listString += "]";
        return listString;
    }

    /**
     * Returns the signal properties as a Map.
     */
    public Map<String, Collection<String>> toMap() {
        Map<String, Collection<String>> params = new HashMap<String, Collection<String>>();
        if (id != null) {
            ArrayList<String> valueList = new ArrayList<String>();
            valueList.add(id);
            params.put("id", valueList);
        }
        if (layoutClassList != null) {
            ArrayList<String> valueList = new ArrayList<String>();
            valueList.add(String.join(",", layoutClassList));
            params.put("layoutClassList", valueList);
        }

        return params;
    }

};


