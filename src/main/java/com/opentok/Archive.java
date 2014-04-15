package com.opentok;

import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;

/**
* Represents an archive of an OpenTok session. 
*/
public class Archive {
    /**
     * Defines values returned by the {@link Archive#getStatus} method.
     */
    public enum ArchiveState {
        /**
         * The archive file is available for download from the OpenTok cloud. You can get the URL of
         * the download file by calling the {@link Archive#getUrl} method.
         */
        available,
        /**
         * The archive file has been deleted.
         */
        deleted,
        /**
         * The recording of the archive failed.
         */
        failed,
        /**
         * The archive recording has started and is in progress.
         */
        started,
        /**
         * The archive recording has stopped, but the file is not available.
         */
        stopped,
        /**
         * The archive file is available at the target S3 bucket you specified using the
         * REST API.
         */
        uploaded,
        /**
         * The archive status is unknown.
         */
        unknown
    }

    private long createdAt = System.currentTimeMillis();
    private int duration = 0;
    private UUID id;
    private String name;
    private int partnerId;
    private String reason = "";
    private String sessionId;
    private int size = 0;
    private ArchiveState status = ArchiveState.unknown;
    private String url;

    protected Archive() {
    }

    /**
     * The time at which the archive was created, in milliseconds since the UNIX epoch.
     */
    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * The duration of the archive, in milliseconds.
     */
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * The archive ID.
     */
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * The name of the archive.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The OpenTok API key associated with the archive.
     */
    public int getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    /**
     * For archives with the status "stopped", this can be set to "90 mins exceeded", "failure", "session ended",
     * or "user initiated". For archives with the status "failed", this can be set to "system failure".
     */
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * The session ID of the OpenTok session associated with this archive.
     */
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /** 
     * The size of the MP4 file. For archives that have not been generated, this value is set to 0.
     */
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * The status of the archive, as defined by the {@link ArchiveState} enum.
     */
    public ArchiveState getStatus() {
        return status;
    }

    public void setStatus(ArchiveState status) {
        this.status = status;
    }

    /**
     * The download URL of the available MP4 file. This is only set for an archive with the status set to "available";
     * for other archives, (including archives wit the status "uploaded") this method returns null. The download URL is
     * obfuscated, and the file is only available from the URL for 10 minutes. To generate a new URL, call
     * the {@link com.opentok.OpenTok#listArchives()} or {@link com.opentok.OpenTok#getArchive(String)} method.
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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