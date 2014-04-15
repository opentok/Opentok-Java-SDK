package com.opentok;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
* Represents an archive of an OpenTok session. 
*/
public class Archive {
    /**
     * Defines values returned by the {@link Archive#getStatus} method.
     */
    public enum Status {
        /**
         * The archive file is AVAILABLE for download from the OpenTok cloud. You can get the URL of
         * the download file by calling the {@link Archive#getUrl} method.
         */
        AVAILABLE,
        /**
         * The archive file has been DELETED.
         */
        DELETED,
        /**
         * The recording of the archive FAILED.
         */
        FAILED,
        /**
         * The archive recording has STARTED and is in progress.
         */
        STARTED,
        /**
         * The archive recording has STOPPED, but the file is not AVAILABLE.
         */
        STOPPED,
        /**
         * The archive file is AVAILABLE at the target S3 bucket you specified using the
         * REST API.
         */
        UPLOADED;

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

    protected Archive() {
    }

    @JsonCreator
    public static Archive makeArchive() {
        return new Archive();
    }

    /**
     * The time at which the archive was created, in milliseconds since the UNIX epoch.
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
     * For archives with the status "STOPPED", this can be set to "90 mins exceeded", "failure", "session ended",
     * or "user initiated". For archives with the status "FAILED", this can be set to "system failure".
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
     * The download URL of the AVAILABLE MP4 file. This is only set for an archive with the status set to "AVAILABLE";
     * for other archives, (including archives wit the status "UPLOADED") this method returns null. The download URL is
     * obfuscated, and the file is only AVAILABLE from the URL for 10 minutes. To generate a new URL, call
     * the {@link com.opentok.OpenTok#listArchives()} or {@link com.opentok.OpenTok#getArchive(String)} method.
     */
    public String getUrl() {
        return url;
    }
    
//    public Archive stop() throws OpenTokException {
//        HashMap<String, String> headers = new HashMap<String, String>();
//        headers.put("content-type", "application/json");
//        String archive = HttpClient.makePostRequest("/v2/partner/" + this.partnerId + "/archive/" + id, headers, null,
//                "{ \"action\" : \"stop\"  }");
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            Archive updatedArchive =  mapper.readValue(archive, Archive.class);
//            this.status = updatedArchive.status;
//            return this;
//        } catch (Exception e) {
//            throw new RequestException(500, "Exception mapping json: " + e.getMessage());
//        }
//    }
    
//    public void delete() throws OpenTokException {
//        HttpClient.makeDeleteRequest("/v2/partner/" + partnerId + "/archive/" + id);
//    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "";
        }
        
    }

}