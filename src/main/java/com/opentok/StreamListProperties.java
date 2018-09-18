package com.opentok;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#setStreamsLayout(String, StreamListProperties)}
 *
 * @see OpenTok#setStreamsLayout(String, StreamListProperties)
 */
public class StreamListProperties {
    private List<StreamProperties> streamList;

    private StreamListProperties(StreamListProperties.Builder builder) {
        this.streamList = builder.streamList;
    }

    /**
     * Use this class to create a StreamListProperties object.
     *
     * @see StreamListProperties
     */
    public static class Builder {
        private List<StreamProperties> streamList = new ArrayList<>();
        /**
         * Call this method to set layout class list of a stream.
         *
         * @param streamProps The StreamProperties object .
         *
         * @return The StreamListProperties.Builder object with the list of StreamProperties setting.
         */
        public StreamListProperties.Builder addStreamProperties (StreamProperties streamProps) {
            this.streamList.add(streamProps);
            return this;
        }

        /**
         * Builds the StreamListProperties object.
         *
         * @return The StreamListProperties object.
         */
        public StreamListProperties build() {
            return new StreamListProperties(this);
        }
    }

    /**
     *   Returns the StreamProperties list
     */
    public List<StreamProperties> getStreamList() {
        return streamList;
    }

}
