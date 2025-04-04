/**
 * OpenTok Java SDK
 * Copyright (C) 2025 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
* Represents an archive of an OpenTok session.
*/
@JsonIgnoreProperties(ignoreUnknown=true)
public class Archive {
    /**
     * Defines values returned by the {@link Archive#getStatus} method.
     */
    public enum Status {
        /**
         * The archive file is available for download from the OpenTok cloud. You can get the URL of
         * the download file by calling the {@link Archive#getUrl} method.
         */
        AVAILABLE,
        /**
         * The archive file has been deleted.
         */
        DELETED,
        /**
         * The recording of the archive failed.
         */
        FAILED,

        /**
         * The archive is in progress and no clients are publishing streams to the session.
         * When an archive is in progress and any client publishes a stream, the status is STARTED.
         * When an archive is PAUSED, nothing is recorded. When a client starts publishing a stream,
         * the recording starts (or resumes). If all clients disconnect from a session that is being
         * archived, the status changes to PAUSED, and after 60 seconds the archive recording stops
         * (and the status changes to STOPPED).
         */
        PAUSED,

        /**
         * The archive recording has started and is in progress.
         */
        STARTED,
        /**
         * The archive recording has stopped, but the file is not available.
         */
        STOPPED,
        /**
         * The archive is available for download from the the upload target
         * Amazon S3 bucket or Windows Azure container you set up for your
         * <a href="https://tokbox.com/account">OpenTok project</a>.
         */
        UPLOADED,

        /**
         * The archive file is no longer available at the OpenTok cloud.
         */
        EXPIRED;

        @JsonValue
        public String toString() {
            return name().toLowerCase();
        }
    }

    /**
     * Defines values used in the
     * {@link ArchiveProperties.Builder#outputMode(com.opentok.Archive.OutputMode)} method
     * and returned by the {@link Archive#getOutputMode} method.
     */
    public enum OutputMode {
        /**
         * All streams in the archive are recorded to a single (composed) file.
         */
        COMPOSED,
        /**
         * Each stream in the archive is recorded to its own individual file.
         */
        INDIVIDUAL;

        @JsonValue public String toString() {
            return super.toString().toLowerCase();
        }
    }

    /**
     * Defines values used in the
     * {@link ArchiveProperties.Builder#streamMode(com.opentok.Archive.StreamMode)} method
     * and returned by the {@link Archive#getStreamMode()} method.
     */
    public enum StreamMode {
        /**
         * Streams will be automatically included in the archive.
         */
        AUTO,
        /**
         * Streams will be included in the archive based on calls to the
         * {@link OpenTok#addArchiveStream(String, String, boolean, boolean)} and
         * {@link OpenTok#removeArchiveStream(String, String)} methods.
         */
        MANUAL;

        @JsonValue
        public String toString() {
            return name().toLowerCase();
        }
    }

    @JsonProperty private long createdAt;
    @JsonProperty private int duration;
    @JsonProperty private String id;
    @JsonProperty private String name;
    @JsonProperty private int partnerId;
    @JsonProperty private String reason;
    @JsonProperty private String sessionId;
    @JsonProperty private long size;
    @JsonProperty private int maxBitrate;
    @JsonProperty private Status status;
    @JsonProperty private String url;
    @JsonProperty private boolean hasVideo = true;
    @JsonProperty private boolean hasAudio = true;
    @JsonProperty private OutputMode outputMode = OutputMode.COMPOSED;
    @JsonProperty private StreamMode streamMode = StreamMode.AUTO;
    @JsonProperty private String password;
    @JsonProperty private String resolution;
    @JsonProperty private String multiArchiveTag;
    @JsonProperty private int quantizationParameter;

    protected Archive() {
    }

    @JsonCreator
    public static Archive makeArchive() {
        return new Archive();
    }

    /**
     * The time at which the archive was created, in milliseconds since the Unix epoch.
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * The duration of the archive, in seconds.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * The archive ID.
     */
    public String getId() {
        return id;
    }

    /**
     * The name of the archive.
     */
    public String getName() {
        return name;
    }

    /**
     * The resolution of the archive.
     */
    public String getResolution() {
        return resolution;
    }

    /**
     * The OpenTok API key associated with the archive.
     */
    public int getPartnerId() {
        return partnerId;
    }

    /**
     * For archives with the status Status.STOPPED or Status.FAILED, this string describes the
     * reason the archive stopped (such as "maximum duration exceeded") or failed.
     */
    public String getReason() {
        return reason;
    }

    /**
     * The session ID of the OpenTok session associated with this archive.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * The size of the MP4 file. For archives that have not been generated, this value is set to 0.
     */
    public long getSize() {
        return size;
    }

    /**
     * The maximum bitrate of the archive, in bits per second.
     *
     * @since 4.15.0
     */
    public int getMaxBitrate() {
        return maxBitrate;
    }

    /**
     * The status of the archive, as defined by the {@link com.opentok.Archive.Status} enum.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * The download URL of the available MP4 file. This is only set for an archive with the status
     * set to Status.AVAILABLE; for other archives, (including archives with the status of
     * Status.UPLOADED) this method returns null. The download URL is obfuscated, and the file
     * is only available from the URL for 10 minutes. To generate a new URL, call the
     * {@link com.opentok.OpenTok#listArchives()} or {@link com.opentok.OpenTok#getArchive(String)}
     * method.
     */
    public String getUrl() {
        return url;
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
     * The output mode to be generated for this archive: <code>composed</code> or <code>individual</code>.
     */
    public OutputMode getOutputMode() {
        return outputMode;
    }

    /**
     * The stream mode to used for selecting streams to be included in this archive:
     * <code>StreamMode.AUTO</code> or <code>StreamMode.MANUAL</code>.
     */
    public StreamMode getStreamMode() {
        return streamMode;
    }
    
    /**
     * The encrypted password if an archive storage was configured to use an encryption key
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the multiArchiveTag if set for the Archive.
     */
    public String getMultiArchiveTag() {
        return multiArchiveTag;
    }

    /**
     * Returns the quantization parameter if set for the Archive.
     *
     * @return The quantization parameter, between 15 and 40.
     * @since 4.16.0
     */
    public int getQuantizationParameter() {
        return quantizationParameter;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "";
        }
    }

}
