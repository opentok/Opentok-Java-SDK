package com.opentok;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    }    /**
     * Use this class to create a StreamProperties object.
     *
     * @see StreamProperties
     */
    public static class Builder {
        private List<StreamProperties> streamList = new ArrayList<>();
        /**
         * Call this method to set layoutClassList of the stream.
         *
         * @param streamProps The stream properties using StreamProperties builder
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


    /**
     * Returns the signal properties as a Map.
     */
    public Map<String, Collection<String>> toMap() {
        Map<String, Collection<String>> params = new HashMap<String, Collection<String>>();
        if (streamList != null) {
            ArrayList<String> valueList = new ArrayList<String>();
            valueList.add(String.join(",", streamList.toString()));
            params.put("items", valueList);
        }
        return params;
    }


}
