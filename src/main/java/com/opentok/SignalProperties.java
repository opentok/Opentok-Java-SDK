/**
 * OpenTok Java SDK
 * Copyright (C) 2019 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#signal(String, SignalProperties)} (String, String)}  method.
 *
 * @see OpenTok#signal(String, SignalProperties) (String, String)
 */
public class SignalProperties {
    private String type = null;
    private String data = null;

    private SignalProperties(SignalProperties.Builder builder) {
        this.type = builder.type;
        this.data = builder.data;
    }
    /**
     * Use this class to create a SignalProperties object.
     *
     * @see SignalProperties
     */
    public static class Builder {
        private String type = null;
        private String data = null;

        /**
         * Call this method to set a type of the signal.
         *
         * @param type The type of the signal
         *
         * @return The SignalProperties.Builder object with the name setting.
         */
        public SignalProperties.Builder type(String type) {
            this.type = type;
            return this;
        }

        /**
         * Call this method to set data of the signal.
         *
         * @param data The type of the signal
         *
         * @return The SignalProperties.Builder object with the data setting.
         */
        public SignalProperties.Builder data(String data) {
            this.data = data;
            return this;
        }

        /**
         * Builds the SignalProperties object.
         *
         * @return The ArchiveSignalPropertiesProperties object.
         */
        public SignalProperties build() {
            return new SignalProperties(this);
        }
    }
    /**
     * Returns the type of the signal
     */
    public String type() {
        return type;
    }
    /**
     *   Returns the type of the signal
     */
    public String data() {
        return data;
    }


    /**
     * Returns the signal properties as a Map.
     */
    public Map<String, Collection<String>> toMap() {
        Map<String, Collection<String>> params = new HashMap<String, Collection<String>>();
        if (null != type) {
            ArrayList<String> valueList = new ArrayList<String>();
            valueList.add(type);
            params.put("type", valueList);
        }
        if (null != data) {
            ArrayList<String> valueList = new ArrayList<String>();
            valueList.add(data);
            params.put("data", valueList);
        }

        return params;
    }

};


