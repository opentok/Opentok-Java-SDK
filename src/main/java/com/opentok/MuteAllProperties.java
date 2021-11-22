/**
 * OpenTok Java SDK
 * Copyright (C) 2021 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#forceMuteAll(String, MuteAllProperties)} method.
 *
 * @see OpenTok#forceMuteAll(String, MuteAllProperties)
 */
public class MuteAllProperties {
    private List<String> excludedStreamIds;

    private MuteAllProperties(MuteAllProperties.Builder builder) {
        this.excludedStreamIds = builder.excludedStreamIds;
    }

    /**
     * Use this class to create a MuteAllProperties object.
     *
     * @see MuteAllProperties
     */
    public static class Builder {
        private List<String> excludedStreamIds = new ArrayList<>();

        /**
         * Call this method to add a list of strings to the excludedStreams list
         *
         * @param ids A List of type {@link String}.
         *
         * @return The MuteAllProperties.Builder object with excludedStreams list.
         */
        public MuteAllProperties.Builder excludedStreamIds(List<String> ids) {
            this.excludedStreamIds.addAll(ids);
            return this;
        }

        /**
         * Builds the MuteAllProperties object.
         *
         * @return The MuteAllProperties object.
         */
        public MuteAllProperties build() {
            return new MuteAllProperties(this);
        }
    }

    /**
     * Returns the excludedStreams List
     *
     * @return the excludedStreams list
     */
    public List<String> getExcludedStreamIds() {
        return this.excludedStreamIds;
    }
}
