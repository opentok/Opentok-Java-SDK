package com.opentok;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#createSession(SessionProperties)} method.
 *
 * @see OpenTok#createSession(com.opentok.SessionProperties properties)
 */
public class MuteAllProperties {
    private List<String> excludedStreams;

    private MuteAllProperties(MuteAllProperties.Builder builder) {
        this.excludedStreams = builder.excludedStreams;
    }

    public static class Builder {
        private List<String> excludedStreams = new ArrayList<>();

        public MuteAllProperties.Builder excludedStreamIds(List<String> ids) {
            this.excludedStreams.addAll(ids);
            return this;
        }

        public MuteAllProperties.Builder excludedStream(List<Stream> streams) {
            this.excludedStreams.addAll(streams.stream().map(stream -> stream.getId()).collect(Collectors.toList()));
            return this;
        }

        public MuteAllProperties.Builder excludedStreamId(String id) {
            this.excludedStreams.add(id);
            return this;
        }

        public MuteAllProperties build() {
            return new MuteAllProperties(this);
        }
    }

    public List<String> getExcludedStreams() {
        return this.excludedStreams;
    }
}
