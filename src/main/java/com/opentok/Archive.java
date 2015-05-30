/**
 * OpenTok Java SDK
 * Copyright (C) 2015 TokBox, Inc.
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
         * The archive file is available at the target Amazon S3 bucket
         * or Windows Azure container you set at the
         * <a href="https://dashboard.tokbox.com">OpenTok dashboard</a>.
         */
        UPLOADED,

        /**
         * The archive file is no longer available at the OpenTok cloud.
         */
        EXPIRED;

        @JsonValue public String toString() {
            return super.toString().toLowerCase();
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

    @JsonProperty private long createdAt;
    @JsonProperty private int duration = 0;
    @JsonProperty private String id;
    @JsonProperty private String name;
    @JsonProperty private int partnerId;
    @JsonProperty private String reason;
    @JsonProperty private String sessionId;
    @JsonProperty private int size = 0;
    @JsonProperty private Status status;
    @JsonProperty private String url;
    @JsonProperty private boolean hasVideo = true;
    @JsonProperty private boolean hasAudio = true;
    @JsonProperty private OutputMode outputMode = OutputMode.COMPOSED;

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
     * The duration of the archive, in milliseconds.
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
     * The OpenTok API key associated with the archive.
     */
    public int getPartnerId() {
        return partnerId;
    }

    /**
     * For archives with the status of Status.STOPPED, this can be set to "90 mins exceeded",
     * "failure", "session ended", or "user initiated". For archives with the status of
     * Status.FAILED, this can be set to "system failure".
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
    public int getSize() {
        return size;
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

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "";
        }
        
    }

}