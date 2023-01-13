/**
 * OpenTok Java SDK
 * Copyright (C) 2023 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.opentok.Archive.OutputMode;
import com.opentok.Archive.StreamMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#startArchive(String sessionId, ArchiveProperties properties)} method.
 *
 * @see OpenTok#createSession(com.opentok.SessionProperties properties)
 */
public class ArchiveProperties {
    private String name;
    private String resolution;
    private String multiArchiveTag;
    private boolean hasAudio;
    private boolean hasVideo;
    private OutputMode outputMode;
    private StreamMode streamMode;
    private ArchiveLayout layout;

    private ArchiveProperties(Builder builder) {
        this.name = builder.name;
        this.resolution = builder.resolution;
        this.hasAudio = builder.hasAudio;
        this.hasVideo = builder.hasVideo;
        this.outputMode = builder.outputMode;
        this.streamMode = builder.streamMode;
        this.layout = builder.layout;
        this.multiArchiveTag = builder.multiArchiveTag;
    }

    /**
     * Used to create an ArchiveProperties object.
     *
     * @see ArchiveProperties
     */
    public static class Builder {
        private String name = null;
        private String resolution = null;
        private String multiArchiveTag = null;
        private boolean hasAudio = true;
        private boolean hasVideo = true;
        private OutputMode outputMode = OutputMode.COMPOSED;
        private StreamMode streamMode = StreamMode.AUTO;
        private ArchiveLayout layout = null;

        /**
         * Sets a name for the archive.
         *
         * @param name The name of the archive. You can use this name to identify the archive. It is a property
         * of the Archive object, and it is a property of archive-related events in the OpenTok JavaScript SDK.
         *
         * @return The ArchiveProperties.Builder object with the name setting.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the resolution of the archive.
         *
         * @param resolution The resolution of the archive, either "640x480" (SD, the default) or
         * "1280x720" (HD). This property only applies to composed archives. If you set this
         * and set the outputMode property to "individual", the call in the API method results in
         * an error.
         *
         * @return The ArchiveProperties.Builder object with the resolution setting.
         */
        public Builder resolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        /**
         * Call this method to include an audio track (<code>true</code>, the default)
         * or not <code>false</code>).
         *
         * @param hasAudio Whether the archive will include an audio track.
         *
         * @return The ArchiveProperties.Builder object with the hasAudio setting.
         */
        public Builder hasAudio(boolean hasAudio) {
            this.hasAudio = hasAudio;
            return this;
        }

        /**
         * Call this method to include an video track (<code>true</code>, the default)
         * or not <code>false</code>).
         *
         * @param hasVideo Whether the archive will include a video track.
         *
         * @return The ArchiveProperties.Builder object with the hasVideo setting.
         */
        public Builder hasVideo(boolean hasVideo) {
            this.hasVideo = hasVideo;
            return this;
        }

        /**
         * Sets the output mode for this archive.
         *
         * @param outputMode Set to a value defined in the {@link Archive.OutputMode} enum.
         *
         * @return The ArchiveProperties.Builder object with the output mode setting.
         */
        public Builder outputMode(OutputMode outputMode) {
            this.outputMode = outputMode;
            return this;
        }

        /**
         * Sets the stream mode for this archive.
         * <p>
         * When streams are selected automatically (<code>StreamMode.AUTO</code>, the default), all
         * streams in the session can be included in the archive. When streams are selected manually
         * (<code>StreamMode.MANUAL</code>), you specify streams to be included based on calls
         * to the {@link OpenTok#addArchiveStream(String, String, boolean, boolean)} and
         * {@link OpenTok#removeArchiveStream(String, String)} methods. With
         * <code>StreamMode.MANUAL</code>, you can specify whether a stream's audio, video, or both
         * are included in the archive. Un both automatic and manual modes, the archive composer
         * includes streams based on
         * <a href="https://tokbox.com/developer/guides/archive-broadcast-layout/#stream-prioritization-rules">stream
         * prioritization rules</a>.
         *
         * @param streamMode Set to a value defined in the {@link Archive.StreamMode} enum.
         *
         * @return The ArchiveProperties.Builder object with the stream mode setting.
         */
        public Builder streamMode(StreamMode streamMode) {
            this.streamMode = streamMode;
            return this;
        }

        /**
         * Sets the layout for a composed archive.
         *
         * @param layout An object of type {@link ArchiveLayout} .
         *
         * @return The ArchiveProperties.Builder object with the layout setting.
         */
        public Builder layout(ArchiveLayout layout) {
            this.layout = layout;
            return this;
        }

        /**
         * Set this to support recording multiple archives for the same session simultaneously.
         * Set this to a unique string for each simultaneous archive of an ongoing session. You must also set this
         * option when manually starting an archive that is automatically archived. If you do
         * not specify a unique multiArchiveTag, you can only record one archive at a time for a given session. See
         * <a href="https://tokbox.com/developer/guides/archiving/#simultaneous-archives">
         * Simultaneous Archives documentation</a>.
         *
         * @param multiArchiveTag A unique archive tag.
         *
         * @return The ArchiveProperties.Builder object with the MultiArchiveTag setting.
         */
        public Builder multiArchiveTag(String multiArchiveTag) {
            this.multiArchiveTag = multiArchiveTag;
            return this;
        }

        /**
         * Builds the ArchiveProperties object.
         *
         * @return The ArchiveProperties object.
         */
        public ArchiveProperties build() {
            return new ArchiveProperties(this);
        }
    }

    /**
     * Returns the name of the archive, which you can use to identify the archive.
     */
    public String name() {
        return name;
    }

    /**
     * Returns the resolution of the archive.
     */
    public String resolution() {
        return resolution;
    }

    /**
     * Returns the multiArchiveTag, if present.
     */
    public String getMultiArchiveTag() {
        return multiArchiveTag;
    }

    /**
     * Whether the archive has a video track (<code>true</code>) or not (<code>false</code>).
     */
    public boolean hasVideo() {
        return hasVideo;
    }

    /**
     * Whether the archive has an audio track (<code>true</code>) or not (<code>false</code>).
     */
    public boolean hasAudio() {
        return hasAudio;
    }

    /**
     * The output mode of the archive.
     */
    public OutputMode outputMode() {
        return outputMode;
    }

    /**
     * The stream mode of the archive.
     */
    public StreamMode streamMode() { return streamMode; }

    /**
     * Returns the custom layout of the archive (composed archives only).
     */
    public ArchiveLayout layout() {
        return layout;
    }

    /**
     * Returns the archive properties as a Map.
     */
    public Map<String, Collection<String>> toMap() {
        Map<String, Collection<String>> params = new HashMap<>();
        if (name != null) {
            ArrayList<String> valueList = new ArrayList<>();
            valueList.add(name);
            params.put("name", valueList);
        }
        if (resolution != null) {
            ArrayList<String> valueList = new ArrayList<>();
            valueList.add(resolution);
            params.put("resolution", valueList);
        }
        ArrayList<String> valueList = new ArrayList<>();
        valueList.add(Boolean.toString(hasAudio));
        params.put("hasAudio", valueList);

        valueList = new ArrayList<>();
        valueList.add(Boolean.toString(hasVideo));
        params.put("hasVideo", valueList);

        valueList = new ArrayList<>();
        valueList.add(outputMode.toString());
        params.put("outputMode", valueList);

        valueList = new ArrayList<>();
        valueList.add(streamMode.toString());
        params.put("streamMode", valueList);

        if (layout != null) {
            valueList = new ArrayList<>();
            valueList.add(layout.toString());
            params.put("layout", valueList);
        }

        if (multiArchiveTag != null) {
            valueList = new ArrayList<>();
            valueList.add(multiArchiveTag);
            params.put("multiArchiveTag", valueList);
        }

        return params;
    }

}
