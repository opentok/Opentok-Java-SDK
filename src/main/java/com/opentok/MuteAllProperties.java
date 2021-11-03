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
    private List<String> excludedStreams;

    private MuteAllProperties(MuteAllProperties.Builder builder) {
        this.excludedStreams = builder.excludedStreams;
    }

    /**
     * Use this class to create a MuteAllProperties object.
     *
     * @see MuteAllProperties
     */
    public static class Builder {
        private List<String> excludedStreams = new ArrayList<>();

        /**
         * Call this method to add a A List of stream IDs for streams to be excluded
         * from the force mute action.
         *
         * @param ids The List of stream IDs,
         *
         * @return The MuteAllProperties.Builder object with excludedStreamIds list.
         */
        public MuteAllProperties.Builder excludedStreamIds(List<String> ids) {
            this.excludedStreams.addAll(ids);
            return this;
        }

        /**
         * Call this method to add a list of Stream to the excludedStreams list
         *
         * @param streams A List of type {@link Stream}.
         *
         * @return The MuteAllProperties.Builder object with excludedStreams list.
         */
        public MuteAllProperties.Builder excludedStreams(List<Stream> streams) {
            this.excludedStreams.addAll(streams.stream().map(stream -> stream.getId()).collect(Collectors.toList()));
            return this;
        }

        /**
         * Call this method to add a stream id to the excludedStreams list
         *
         * @param id An object of type {@link String}.
         *
         * @return The MuteAllProperties.Builder object with excludedStreams list.
         */
        public MuteAllProperties.Builder excludedStreamId(String id) {
            this.excludedStreams.add(id);
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
    public List<String> getExcludedStreams() {
        return this.excludedStreams;
    }
}
