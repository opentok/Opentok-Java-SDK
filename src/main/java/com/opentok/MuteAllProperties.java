/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
 * http://www.tokbox.com
 * <p>
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import java.util.ArrayList;
import java.util.List;

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
         * Call this method to add a List of stream IDs for streams to be excluded
         * from the force mute action.
         *
         * @param ids The List of stream IDs.
         *
         * @return The MuteAllProperties.Builder object with excludedStreamIds list.
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
     * Returns the excludedStreams list. This is a list of stream IDs for
     * streams to be excluded from the force mute action.
     *
     * @return The list of stream IDs.
     */
    public List<String> getExcludedStreamIds() {
        return this.excludedStreamIds;
    }
}
