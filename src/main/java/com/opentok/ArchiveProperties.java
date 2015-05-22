/**
 * OpenTok Java SDK
 * Copyright (C) 2015 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.opentok.Archive.OutputMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#createSession(SessionProperties)} method.
 *
 * @see OpenTok#createSession(com.opentok.SessionProperties properties)
 */
public class ArchiveProperties {


    private String name = null;
    private boolean hasAudio;
    private boolean hasVideo;
    private OutputMode outputMode;

    private ArchiveProperties(Builder builder) {
        this.name = builder.name;
        this.hasAudio = builder.hasAudio;
        this.hasVideo = builder.hasVideo;
        this.outputMode = builder.outputMode;
    }

    /**
     * Use this class to create a ArchiveProperties object.
     *
     * @see ArchiveProperties
     */
    public static class Builder {
        private String name = null;
        private boolean hasAudio = true;
        private boolean hasVideo = true;
        private OutputMode outputMode = OutputMode.COMPOSED;
        

        /**
         * Call this method to set a name to the archive.
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
         * Call this method to include an audio track (<code>true</code>) or not <code>false</code>).
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
         * Call this method to include an video track (<code>true</code>) or not <code>false</code>).
         *
         * @param hasVideo Whether the archive will include an video track.
         *
         * @return The ArchiveProperties.Builder object with the hasVideo setting.
         */
        public Builder hasVideo(boolean hasVideo) {
            this.hasVideo = hasVideo;
            return this;
        }        

        /**
         * Call this method to choose the output mode to be generated for this archive.
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
         * Builds the ArchiveProperties object.
         *
         * @return The ArchiveProperties object.
         */
        public ArchiveProperties build() {
            return new ArchiveProperties(this);
        }
    }
    /**
     * Returns the name of the archive, which you can use to identify the archive
     */
    public String name() {
        return name;
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
     * Returns the archive properties as a Map.
     */
    public Map<String, Collection<String>> toMap() {
        Map<String, Collection<String>> params = new HashMap<String, Collection<String>>();
        if (null != name) {
            ArrayList<String> valueList = new ArrayList<String>();
            valueList.add(name);
            params.put("name", valueList);
        }
        ArrayList<String> valueList = new ArrayList<String>();
        valueList.add(Boolean.toString(hasAudio));
        params.put("hasAudio", valueList);
        
        valueList = new ArrayList<String>();
        valueList.add(Boolean.toString(hasVideo));
        params.put("hasVideo", valueList);
        
        valueList = new ArrayList<String>();
        valueList.add(outputMode.toString());
        params.put("outputMode", valueList);

        return params;
    }

};
