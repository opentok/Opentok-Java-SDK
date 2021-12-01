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
    private boolean active;
    private List<String> excludedStreamIds;

    private MuteAllProperties(MuteAllProperties.Builder builder) {
        this.active = builder.active;
        this.excludedStreamIds = builder.excludedStreamIds;
    }

    /**
     * Use this class to create a MuteAllProperties object.
     *
     * @see MuteAllProperties
     */
    public static class Builder {
        private boolean active;
        private List<String> excludedStreamIds = new ArrayList<>();

        /**
         * Call this method to set the active flag.
         *
         * @param active Whether streams published after this call, in addition to the current streams in the session, should be muted
         *
         * @return The MuteAllProperties.Builder object with active property set.
         */
        public MuteAllProperties.Builder active(boolean active) {
            this.active = active;
            return this;
        }

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
     * Returns the active property.
     *
     * @return The active property
     */
    public boolean getActive() {
        return this.active;
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
