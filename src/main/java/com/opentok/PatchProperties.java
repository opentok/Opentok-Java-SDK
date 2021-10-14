/**
 * OpenTok Java SDK
 * Copyright (C) 2021 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#patchArchive(String, PatchProperties)} and
 * {@link OpenTok#patchBroadcast(String, PatchProperties)} method
 * 
 * @see OpenTok#patchArchive(String, PatchProperties) 
 * @see OpenTok#patchBroadcast(String, PatchProperties)
 */
public class PatchProperties {
    private boolean hasAudio;
    private boolean hasVideo;
    private String addStream;
    private String removeStream;

    PatchProperties(Builder builder) {
        this.hasAudio = builder.hasAudio;
        this.hasVideo = builder.hasVideo;
        this.addStream = builder.addStream;
        this.removeStream = builder.removeStream;
    }

    /**
     * Use this class to create a PatchProperty object.
     *
     * @see PatchProperties
     */
    public static class Builder {
        private boolean hasAudio = true;
        private boolean hasVideo = true;
        private String addStream;
        private String removeStream;

        /**
         * Call this method to set hasAudio property
         *
         * @param hasAudio Whether the stream has Audio or not.
         *
         * @return the Builder object with the hasAudio setting.
         */
        public Builder hasAudio(boolean hasAudio) {
            this.hasAudio = hasAudio;
            return this;
        }

        /**
         * Call this method to set hasVideo property
         *
         * @param hasVideo Whether the stream has Video or not.
         *
         * @return the Builder object with the hasVideo setting.
         */
        public Builder hasVideo(boolean hasVideo) {
            this.hasVideo = hasVideo;
            return this;
        }

        /**
         * Call this method to set addStream property
         *
         * @param streamID which stream to add to archive.
         *
         * @return the Builder object with the addStream setting.
         */
        public Builder addStream(String streamID) {
            this.addStream = streamID;
            return this;
        }

        /**
         * Call this method to set addStream property
         *
         * @param streamID which stream to remove from archive.
         *
         * @return the Builder object with the removeStream setting.
         */
        public Builder removeStream(String streamID) {
            this.removeStream = streamID;
            return this;
        }

        /**
         * Builds the PatchProperties object
         *
         * @return the PatchProperties object
         */
        public PatchProperties build() {
            return new PatchProperties(this);
        }
    }

    /**
     * @return the hasAudio setting.
     */
    public boolean hasAudio() { return hasAudio; }

    /**
     * @return the hasVideo setting.
     */
    public boolean hasVideo() { return hasVideo; }

    /**
     * @return the addStream setting.
     */
    public String addStream() { return addStream; }

    /**
     * @return the removeStream setting.
     */
    public String removeStream() { return removeStream; }
}
