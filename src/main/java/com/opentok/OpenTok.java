/**
 * OpenTok Java SDK
 * Copyright (C) 2025 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.opentok.constants.DefaultUserAgent;
import com.opentok.exception.InvalidArgumentException;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.RequestException;
import com.opentok.util.Crypto;
import com.opentok.util.HttpClient;
import com.opentok.util.HttpClient.ProxyAuthScheme;
import org.apache.commons.lang.StringUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Contains methods for creating OpenTok sessions, generating tokens, and working with archives.
 * <p>
 * To create a new OpenTok object, call the OpenTok constructor with your OpenTok API key
 * and the API secret for your <a href="https://tokbox.com/account">Vonage Video API account</a>. Do not publicly share
 * your API secret. You will use it with the OpenTok constructor (only on your web
 * server) to create OpenTok sessions.
 * <p>
 * Be sure to include the entire OpenTok server SDK on your web server.
 */
public class OpenTok {
    private final int apiKey;
    private final String apiSecret, applicationId;
    private final Path privateKeyPath;
    protected HttpClient client;

    protected static final ObjectReader
        archiveReader = new ObjectMapper().readerFor(Archive.class),
        archiveListReader = new ObjectMapper().readerFor(ArchiveList.class),
        createdSessionReader = new ObjectMapper().readerFor(CreatedSession[].class),
        streamReader = new ObjectMapper().readerFor(Stream.class),
        streamListReader = new ObjectMapper().readerFor(StreamList.class),
        sipReader = new ObjectMapper().readerFor(Sip.class),
        broadcastReader = new ObjectMapper().readerFor(Broadcast.class),
        renderReader = new ObjectMapper().readerFor(Render.class),
        renderListReader = new ObjectMapper().readerForListOf(Render.class),
        connectReader = new ObjectMapper().readerFor(AudioConnector.class),
        captionReader = new ObjectMapper().readerFor(Caption.class);

    /**
     * Creates an OpenTok object.
     *
     * @param apiKey Your OpenTok API key. (See your <a href="https://tokbox.com/account">Vonage Video API account page</a>.)
     * @param apiSecret Your OpenTok API secret. (See your <a href="https://tokbox.com/account">Vonage Video API account page</a>.)
     */
    public OpenTok(int apiKey, String apiSecret) {
        this(apiKey, apiSecret, null, null, new HttpClient.Builder(apiKey, apiSecret).build());
    }

    /**
     * Creates an OpenTok object for use with the
     * <a href=https://developer.vonage.com/en/api/video>Vonage Video API</a>. This is intended as a short-term step
     * towards full migration to Vonage. See the
     * <a href=https://developer.vonage.com/en/video/transition-guides/server-sdks/java>Java SDK transition guide</a>
     * for details.
     *
     * @param applicationId Your Vonage application UUID with video capabilities enabled.
     * @param privateKeyPath Absolute path to the private key for your application.
     *
     * @since 4.15.0
     */
    public OpenTok(String applicationId, Path privateKeyPath) {
        this(0, null, applicationId, privateKeyPath, new HttpClient.Builder(applicationId, privateKeyPath).build());
    }

    private OpenTok(int apiKey, String apiSecret, String applicationId, Path privateKeyPath, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret != null ? apiSecret.trim() : null;
        this.applicationId = applicationId;
        this.privateKeyPath = privateKeyPath;
        this.client = httpClient;
    }

    /**
     * Creates a token for connecting to an OpenTok session. In order to authenticate a user
     * connecting to an OpenTok session, the client passes a token when connecting to the session.
     * <p>
     * The following example shows how to obtain a token that has a role of "subscriber" and
     * that has a connection metadata string:
     * <p>
     * <pre>
     * import com.opentok.Role;
     * import com.opentok.TokenOptions;
     *
     * class Test {
     *     public static void main(String args[]) throws OpenTokException {
     *         int API_KEY = 0; // Replace with your OpenTok API key (see https://tokbox.com/account).
     *         String API_SECRET = ""; // Replace with your OpenTok API secret.
     *         OpenTok sdk = new OpenTok(API_KEY, API_SECRET);
     *
     *         //Generate a basic session. Or you could use an existing session ID.
     *         String sessionId = System.out.println(sdk.createSession());
     *
     *         // Replace with meaningful metadata for the connection.
     *         String connectionMetadata = "username=Bob,userLevel=4";
     *
     *         // Use the Role value appropriate for the user.
     *         String role = Role.SUBSCRIBER;
     *
     *         // Generate a token:
     *         TokenOptions options = new TokenOptions.Buider().role(role).data(connectionMetadata).build();
     *         String token = sdk.generateToken(sessionId, options);
     *         System.out.println(token);
     *     }
     * }
     * </pre>
     * <p>
     * For testing, you can also generate tokens by logging in to your <a href="https://tokbox.com/account">Vonage Video API account</a>.
     *
     * @param sessionId The session ID corresponding to the session to which the user will connect.
     *
     * @param tokenOptions This TokenOptions object defines options for the token.
     * These include the following:
     *
     * <ul>
     *    <li>The role of the token (subscriber, publisher, or moderator)</li>
     *    <li>The expiration time of the token</li>
     *    <li>Connection data describing the end-user</li>
     * </ul>
     *
     * @return The token string.
     */
    public String generateToken(String sessionId, TokenOptions tokenOptions) throws OpenTokException {
        Session session;
        if (sessionId == null || sessionId.isEmpty()) {
            throw new InvalidArgumentException("Session not valid");
        }

        if (privateKeyPath == null && apiSecret != null) {
            List<String> sessionIdParts;
            try {
                sessionIdParts = Crypto.decodeSessionId(sessionId);
            }
            catch (UnsupportedEncodingException e) {
                throw new InvalidArgumentException("Session ID was not valid");
            }
            if (!sessionIdParts.contains(Integer.toString(apiKey))) {
                throw new InvalidArgumentException("Session ID was not valid");
            }
            session = new Session(sessionId, apiKey, apiSecret);
        }
        else {
            session = new Session(sessionId, applicationId, privateKeyPath);
        }

        return session.generateToken(tokenOptions);
    }

    /**
     * Creates a token for connecting to an OpenTok session, using the default settings. The default
     * settings are the following:
     *
     * <ul>
     *   <li>The token is assigned the role of publisher.</li>
     *   <li>The token expires 24 hours after it is created.</li>
     *   <li>The token includes no connection data.</li>
     * </ul>
     *
     * <p>
     * The following example shows how to generate a token that has the default settings:
     * <p>
     * <pre>
     * import com.opentok.OpenTok;
     *
     * class Test {
     *     public static void main(String argv[]) throws OpenTokException {
     *         int API_KEY = 0; // Replace with your OpenTok API key (see https://tokbox.com/account).
     *         String API_SECRET = ""; // Replace with your OpenTok API secret.
     *         OpenTok sdk = new OpenTok(API_KEY, API_SECRET);
     *
     *         //Generate a basic session. Or you could use an existing session ID.
     *         String sessionId = System.out.println(sdk.createSession().getSessionId());
     *
     *         String token = sdk.generateToken(sessionId);
     *         System.out.println(token);
     *     }
     * }
     * </pre>
     * @param sessionId The session ID corresponding to the session to which the user will connect.
     *
     * @return The token string.
     *
     * @see #generateToken(String, TokenOptions)
     */
    public String generateToken(String sessionId) throws OpenTokException {
        return generateToken(sessionId, new TokenOptions.Builder().build());
    }

    /**
     * Creates a new OpenTok session.
     * <p>
     * For example, when using the OpenTok.js library, use the session ID when calling the
     * <a href="http://tokbox.com/opentok/libraries/client/js/reference/OT.html#initSession">
     * OT.initSession()</a> method (to initialize an OpenTok session).
     * <p>
     * OpenTok sessions do not expire. However, authentication tokens do expire (see the
     * {@link #generateToken(String, TokenOptions)} method). Also note that sessions cannot
     * explicitly be destroyed.
     * <p>
     * A session ID string can be up to 255 characters long.
     * <p>
     * Calling this method results in an {@link com.opentok.exception.OpenTokException} in
     * the event of an error. Check the error message for details.
     * <p>
     * The following code creates a session that attempts to send streams directly between clients
     * (falling back to use the OpenTok TURN server to relay streams if the clients cannot connect):
     *
     * <pre>
     * import com.opentok.MediaMode;
     * import com.opentok.OpenTok;
     * import com.opentok.Session;
     * import com.opentok.SessionProperties;
     *
     * class Test {
     *     public static void main(String argv[]) throws OpenTokException {
     *         int API_KEY = 0; // Replace with your OpenTok API key.
     *         String API_SECRET = ""; // Replace with your OpenTok API secret.
     *         OpenTok sdk = new OpenTok(API_KEY, API_SECRET);
     *
     *         SessionProperties sp = new SessionProperties().Builder()
     *           .mediaMode(MediaMode.RELAYED).build();
     *
     *         Session session = sdk.createSession(sp);
     *         System.out.println(session.getSessionId());
     *     }
     * }
     * </pre>
     *
     * You can also create a session using the <a href="http://www.tokbox.com/opentok/api/#session_id_production">OpenTok
     * REST API</a> or or by logging in to your
     * <a href="https://tokbox.com/account">Vonage Video API account</a>.
     *
     * @param properties This SessionProperties object defines options for the session.
     * These include the following:
     *
     * <ul>
     *    <li>Whether the session's streams will be transmitted directly between peers or
     *    using the OpenTok Media Router.</li>
     *
     *    <li>A location hint for the location of the OpenTok server to use for the session.</li>
     * </ul>
     *
     * @return A Session object representing the new session. Call the <code>getSessionId()</code>
     * method of the Session object to get the session ID, which uniquely identifies the
     * session. You will use this session ID in the client SDKs to identify the session.
     */
    public Session createSession(SessionProperties properties) throws OpenTokException {
        final SessionProperties _properties = properties != null ? properties : new SessionProperties.Builder().build();
        final Map<String, List<String>> params = _properties.toMap();
        final String response = client.createSession(params);

        try {
            CreatedSession[] sessions = createdSessionReader.readValue(response);
            // A bit ugly, but API response should include an array with one session
            if (sessions.length != 1) {
                throw new OpenTokException(String.format("Unexpected number of sessions created %d", sessions.length));
            }
            return new Session(sessions[0].getId(), apiKey, apiSecret, _properties);
        } catch (IOException e) {
            throw new OpenTokException("Cannot create session. Could not read the response: " + response);
        }
    }

    /**
     * Creates an OpenTok session with the default settings:
     *
     * <p>
     * <ul>
     *     <li>The media mode is "relayed". The session will attempt to transmit streams
     *        directly between clients. If two clients cannot send and receive each others'
     *        streams, due to firewalls on the clients' networks, their streams will be
     *        relayed  using the OpenTok TURN Server.</li>
     *     <li>The session uses the first client connecting to determine the location of the
     *        OpenTok server to use.</li>
     * </ul>
     *
     * <p>
     * The following example creates a session that uses the default settings:
     *
     * <pre>
     * import com.opentok.OpenTok;
     * import com.opentok.SessionProperties;
     *
     * class Test {
     *     public static void main(String argv[]) throws OpenTokException {
     *         int API_KEY = 0; // Replace with your OpenTok API key.
     *         String API_SECRET = ""; // Replace with your OpenTok API secret.
     *         OpenTok sdk = new OpenTok(API_KEY, API_SECRET);
     *
     *         String sessionId = sdk.createSession();
     *         System.out.println(sessionId);
     *     }
     * }
     * </pre>
     *
     * @return A Session object representing the new session. Call the <code>getSessionId()</code>
     * method of the Session object to get the session ID, which uniquely identifies the
     * session. You will use this session ID in the client SDKs to identify the session.
     *
     * @see #createSession(SessionProperties)
     */
    public Session createSession() throws OpenTokException {
        return createSession(null);
    }

    /**
     * Sends a signal to all clients connected to a session.
     *
     * <p>
     * For more information, see the
     * <a href="https://tokbox.com/developer/guides/signaling/">Signaling developer guide</a>.
     *
     * @param sessionId The session ID.
     * @param props The SignalProperties object that defines the data and type of the signal.
     */
    public void signal(String sessionId, SignalProperties props) throws OpenTokException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new InvalidArgumentException("Session string null or empty");
        }
        client.signal(sessionId, null, props);
    }

    /**
     * Sends a signal to a specific client connected to a session.
     *
     * <p>
     * For more information, see the
     * <a href="https://tokbox.com/developer/guides/signaling/">Signaling developer guide</a>.
     *
     * @param sessionId The session ID.
     * @param connectionId The connection ID of the client to receive the signal.
     * @param props The SignalProperties object that defines the data and type of the signal.
     */
    public void signal(String sessionId, String connectionId, SignalProperties props) throws OpenTokException {
        if (sessionId == null || sessionId.isEmpty() || connectionId == null || connectionId.isEmpty()) {
            throw new InvalidArgumentException("Session or Connection string null or empty");
        }
        client.signal(sessionId, connectionId, props);
    }

    /**
     * Gets an {@link Archive} object for the given archive ID.
     *
     * @param archiveId The archive ID.
     * @return The {@link Archive} object.
     */
    public Archive getArchive(String archiveId) throws OpenTokException {
        String archive = client.getArchive(archiveId);
        try {
            return archiveReader.readValue(archive);
        } catch (Exception e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Returns a List of {@link Archive} objects, representing archives that are both
     * both completed and in-progress, for your API key. This list is limited to 1000 archives
     * starting with the first archive recorded. For a specific range of archives, call
     * {@link #listArchives(int offset, int count)}.
     *
     * @return A List of {@link Archive} objects.
     */
    public ArchiveList listArchives() throws OpenTokException {
        return listArchives("", 0, 1000);
    }

    /**
     * Returns a List of {@link Archive} objects, representing archives that are both
     * both completed and in-progress, for your API key.
     *
     * @param sessionId The sessionid of the session which started or automatically enabled archiving.
     *                  If the session is null or empty it will be omitted.
     * @return A List of {@link Archive} objects.
     */
    public ArchiveList listArchives(String sessionId) throws OpenTokException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new InvalidArgumentException("Session Id cannot be null or empty");
        }
        return listArchives(sessionId, 0, 1000);
    }

    /**
     * Returns a List of {@link Archive} objects, representing archives that are both
     * both completed and in-progress, for your API key.
     *
     * @param offset The index offset of the first archive. 0 is offset of the most recently started
     * archive.
     * 1 is the offset of the archive that started prior to the most recent archive.
     * @param count The number of archives to be returned. The maximum number of archives returned
     * is 1000.
     * @return A List of {@link Archive} objects.
     */
    public ArchiveList listArchives(int offset, int count) throws OpenTokException {
        return listArchives("", offset, count);
    }

    /**
     * Returns a List of {@link Archive} objects, representing archives that are both
     * both completed and in-progress, for your API key.
     *
     * @param offset The index offset of the first archive. 0 is offset of the most recently started
     * archive.
     * 1 is the offset of the archive that started prior to the most recent archive.
     * @param count The number of archives to be returned. The maximum number of archives returned
     * is 1000.
     * @param sessionId The sessionid of the session which started or automatically enabled archiving.
     *
     * @return A List of {@link Archive} objects.
     */
    public ArchiveList listArchives(String sessionId, int offset, int count) throws OpenTokException {
        String archives = client.getArchives(sessionId, offset, count);
        try {
            return archiveListReader.readValue(archives);
        } catch (JsonProcessingException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Starts archiving an OpenTok session. This version of the <code>startArchive()</code> method
     * lets you disable audio or video recording.
     * <p>
     * Clients must be actively connected to the OpenTok session for you to successfully start
     * recording an archive.
     * <p>
     * You can only record one archive at a time for a given session. You can only record archives
     * of sessions that use the OpenTok Media Router (sessions with the
     * <a href="https://tokbox.com/developer/guides/create-session/#media-mode">media mode</a>
     * set to routed); you cannot archive sessions with the media mode set to relayed.
     * <p>
     * For more information on archiving, see the
     * <a href="https://tokbox.com/developer/guides//archiving/">OpenTok archiving</a>
     * developer guide.
     *
     * @param sessionId The session ID of the OpenTok session to archive.
     *
     * @param properties This ArchiveProperties object defines options for the archive.
     *
     * @return The Archive object. This object includes properties defining the archive, including the archive ID.
     */
    public Archive startArchive(String sessionId, ArchiveProperties properties) throws OpenTokException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new InvalidArgumentException("Session not valid");
        }
        boolean hasResolution = properties != null && properties.resolution() != null && !properties.resolution().isEmpty();
        if (properties != null && properties.outputMode().equals(Archive.OutputMode.INDIVIDUAL) && hasResolution) {
            throw new InvalidArgumentException("The resolution cannot be specified for individual output mode.");
        }
        // TODO: do validation on sessionId and name
        String archive = client.startArchive(sessionId, properties);
        try {
            return archiveReader.readValue(archive);
        } catch (Exception e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    public Archive startArchive(String sessionId) throws OpenTokException {
        return startArchive(sessionId, new ArchiveProperties.Builder().build());
    }

    public Archive startArchive(String sessionId, String name) throws OpenTokException {
        ArchiveProperties properties = new ArchiveProperties.Builder().name(name).build();
        return startArchive(sessionId, properties);
    }

    /**
     * Stops an OpenTok archive that is being recorded.
     * <p>
     * Archives automatically stop recording after 120 minutes or when all clients have disconnected
     * from the session being archived.
     *
     * @param archiveId The archive ID of the archive you want to stop recording.
     * @return The Archive object corresponding to the archive being stopped.
     */
    public Archive stopArchive(String archiveId) throws OpenTokException {

        String archive = client.stopArchive(archiveId);
        try {
            return archiveReader.readValue(archive);
        } catch (Exception e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Deletes an OpenTok archive.
     * <p>
     * You can only delete an archive which has a status of "available" or "uploaded". Deleting an
     * archive removes its record from the list of archives. For an "available" archive, it also
     * removes the archive file, making it unavailable for download.
     *
     * @param archiveId The archive ID of the archive you want to delete.
     */
    public void deleteArchive(String archiveId) throws OpenTokException {
        client.deleteArchive(archiveId);
    }

    /**
     * Adds a stream to an OpenTok archive.
     * <p>
     * This method only works for an archive that has a {@link com.opentok.Archive.StreamMode} set
     * to <code>StreamMode.MANUAL</code>.
     * <p>
     * You can call this method repeatedly with the same stream ID to enable and disable audio or
     * video, based on the <code>hasAudio</code> and <code>hasVideo</code> parameter values.
     *
     * @param archiveId The archive ID.
     * @param streamId The stream ID.
     * @param hasAudio Whether the stream should have audio enabled in the archive.
     * @param hasVideo Whether the stream should have video enabled in the archive.
     *
     * @throws OpenTokException
     */
    public void addArchiveStream(String archiveId, String streamId, boolean hasAudio, boolean hasVideo) throws OpenTokException {
        client.patchArchive(archiveId, streamId, null, hasAudio, hasVideo);
    }

    /**
     * Removes a stream from an Opentok archive.
     * <p>
     * This method only works for an archive that has a {@link com.opentok.Archive.StreamMode} set
     * to <code>StreamMode.MANUAL</code>.
     *
     * @param archiveId The archive ID.
     * @param streamId The stream ID.
     *
     * @throws OpenTokException
     */
    public void removeArchiveStream(String archiveId, String streamId) throws OpenTokException {
        client.patchArchive(archiveId, null, streamId, false, false);
    }

    /**
     * Sets the layout type for a composed archive. For a description of layout types, see
     *  <a href="https://tokbox.com/developer/guides/archiving/layout-control.html">Customizing
     *  the video layout for composed archives</a>.
     *
     * @param archiveId {String} The archive ID.
     *
     * @param properties The ArchiveProperties object defining the archive layout.
     */
    public void setArchiveLayout(String archiveId, ArchiveProperties properties) throws OpenTokException {
        if (StringUtils.isEmpty(archiveId) || properties == null) {
            throw new InvalidArgumentException("ArchiveId is not valid or properties are null");
        }
        client.setArchiveLayout(archiveId, properties);
    }

    /**
     * Starts a live streaming broadcast for an OpenTok session.
     * This broadcasts the session to an HLS (HTTP live streaming) or to RTMP streams.
     * <p>
     * To successfully start broadcasting a session, at least one client must be connected to the session.
     * <p>
     * You can only have one active live streaming broadcast at a time for a session
     * (however, having more than one would not be useful).
     * The live streaming broadcast can target one HLS endpoint and up to five RTMP servers simulteneously for a session.
     * You can only start live streaming for sessions that use the OpenTok Media Router (with the media mode set to routed);
     * you cannot use live streaming with sessions that have the media mode set to relayed OpenTok Media Router. See
     * <a href="https://tokbox.com/developer/guides/create-session/#media-mode">The OpenTok Media Router and media modes.</a>
     * <p>
     * For more information on broadcasting, see the
     * <a href="https://tokbox.com/developer/guides/broadcast/">Broadcast developer guide.</a>
     *
     * @param sessionId The session ID of the OpenTok session to broadcast.
     *
     * @param properties The BroadcastProperties object defines options for the broadcast.
     *
     * @return The Broadcast object. This object includes properties defining the broadcast,
     * including the broadcast ID.
     */
    public Broadcast startBroadcast(String sessionId, BroadcastProperties properties) throws OpenTokException {
        if (StringUtils.isEmpty(sessionId) || properties == null) {
            throw new InvalidArgumentException("Session not valid or broadcast properties is null");
        }

        String broadcast = client.startBroadcast(sessionId, properties);
        try {
            return broadcastReader.readValue(broadcast);
        } catch (Exception e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Stops a live streaming broadcast of an OpenTok session.
     * Note that broadcasts automatically stop 120 minutes after they are started.
     * <p>
     * For more information on broadcasting, see the
     * <a href="https://tokbox.com/developer/guides/broadcast/">Broadcast developer guide.</a>
     *
     * @param broadcastId The broadcast ID of the broadcasting session
     *
     * @return The Broadcast object. This object includes properties defining the archive, including the archive ID.
     */
    public Broadcast stopBroadcast(String broadcastId) throws OpenTokException {
        if (StringUtils.isEmpty(broadcastId)) {
            throw new InvalidArgumentException("Broadcast id is null or empty");
        }
        String broadcast = client.stopBroadcast(broadcastId);
        try {
            return broadcastReader.readValue(broadcast);
        } catch (Exception e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Gets an {@link Broadcast} object for the given broadcast ID.
     *
     * @param broadcastId The broadcast ID.
     * @return The {@link Broadcast} object.
     */
    public Broadcast getBroadcast(String broadcastId) throws OpenTokException {
        if (StringUtils.isEmpty(broadcastId)) {
            throw new InvalidArgumentException("Broadcast id is null or empty");
        }
        String stream = client.getBroadcast(broadcastId);
        try {
            return broadcastReader.readValue(stream);
        } catch (Exception e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Sets the layout type for the broadcast. For a description of layout types, see
     * <a href="hhttps://tokbox.com/developer/guides/broadcast/live-streaming/#configuring-video-layout-for-opentok-live-streaming-broadcasts">Configuring
     * video layout for OpenTok live streaming broadcasts</a>.
     *
     * @param broadcastId {String} The broadcast ID.
     *
     * @param properties This BroadcastProperties object that defines layout options for
     * the broadcast.
     */
    public void setBroadcastLayout(String broadcastId, BroadcastProperties properties) throws OpenTokException {
        if (StringUtils.isEmpty(broadcastId) || properties == null) {
            throw new InvalidArgumentException("BroadcastId is not valid or properties are null");
        }
        client.setBroadcastLayout(broadcastId, properties);
    }

    /**
     * Adds a stream to an Opentok broadcast.
     * <p>
     * This method only works for an archive that has a {@link com.opentok.Archive.StreamMode} set
     * to <code>StreamMode.MANUAL</code>.
     * <p>
     * You can call this method repeatedly with the same stream ID to enable and disable audio or
     * video, based on the <code>hasAudio</code> and <code>hasVideo</code> parameter values.
     *
     * @param broadcastId The broadcast ID.
     * @param streamId The stream ID.
     * @param hasAudio Whether the stream should have audio enabled in the broadcast.
     * @param hasVideo Whether the stream should have video enabled in the broadcast.
     *
     * @throws OpenTokException
     */
    public void addBroadcastStream(String broadcastId, String streamId, boolean hasAudio, boolean hasVideo) throws OpenTokException {
        client.patchBroadcast(broadcastId, streamId, null, hasAudio, hasVideo);
    }

    /**
     * Removes a stream from an Opentok broadcast.
     * <p>
     * This method only works for an archive that has a {@link com.opentok.Archive.StreamMode} set
     * to <code>StreamMode.MANUAL</code>.
     *
     * @param broadcastId The broadcast ID.
     * @param streamId The stream ID.
     *
     * @throws OpenTokException
     */
    public void removeBroadcastStream(String broadcastId, String streamId) throws OpenTokException {
        client.patchBroadcast(broadcastId, null, streamId, false, false);
    }

    /**
     * Sets the layout class list for streams in a session. Layout classes are used in
     * the layout for composed archives and live streaming broadcasts. For more information, see
     * <a href="https://tokbox.com/developer/guides/archiving/layout-control.html">Customizing
     * the video layout for composed archives</a> and
     * <a href="https://tokbox.com/developer/guides/broadcast/live-streaming/#configuring-video-layout-for-opentok-live-streaming-broadcasts">Configuring
     * video layout for OpenTok live streaming broadcasts</a>.
     *
     * <p>
     * You can set the initial layout class list for streams published by a client when you generate
     * used by the client. See the {@link #generateToken(String, TokenOptions)} method.
     *
     * @param sessionId {String} The session ID of the session the streams belong to.
     *
     * @param properties This StreamListProperties object defines class lists for one or more
     * streams in the session.
     */
    public void setStreamLayouts(String sessionId, StreamListProperties properties) throws OpenTokException {
        if (StringUtils.isEmpty(sessionId) || properties == null) {
            throw new InvalidArgumentException("SessionId is not valid or properties are null");
        }
        client.setStreamLayouts(sessionId, properties);
    }

    /**
     * Disconnect a client from an OpenTok session
     * <p>
     * Use this API to forcibly terminate a connection of a session.
     *
     * @param sessionId The session ID of the connection
     * @param  connectionId The connection ID to disconnect
     */
    public void forceDisconnect(String sessionId, String connectionId) throws OpenTokException {
        if (sessionId == null || sessionId.isEmpty() || connectionId == null || connectionId.isEmpty()) {
            throw new InvalidArgumentException("Session or Connection string null or empty");
        }
        client.forceDisconnect(sessionId, connectionId);
    }

    /**
     * Force the publisher of a specific stream to mute its audio.
     * <p>
     * For more information, see
     * <a href="https://tokbox.com/developer/guides/moderation/#force_mute">Muting the audio of streams in a session</a>.
     *
     * @param sessionId The session ID.
     * @param  streamId The stream ID.
     * @throws OpenTokException
     *
     * @see #forceMuteAll(String, MuteAllProperties)
     */
    public void forceMuteStream(String sessionId, String streamId) throws OpenTokException {
        if (sessionId == null || sessionId.isEmpty() || streamId == null || streamId.isEmpty()) {
            throw new InvalidArgumentException("Session or Connection string null or empty");
        }
        client.forceMuteStream(sessionId, streamId);
    }

    /**
     * Forces all streams (except for an optional array of streams) in a session
     * to mute published audio.
     * <p>
     * In addition to existing streams, any streams that are published after the call
     * to this method are published with audio muted. You can remove the mute state of
     * a session by calling the {@link #disableForceMute(String) OpenTok.disableForceMute()} method.
     * <p>
     * For more information, see
     * <a href="https://tokbox.com/developer/guides/moderation/#force_mute">Muting the audio of streams in a session</a>.
     *
     * @param sessionId The session ID.
     * @param properties Defines a list of stream IDs for streams that should be excluded
     *                   from the mute action.
     * @throws OpenTokException
     *
     * @see #forceMuteStream(String, String)
     * @see #disableForceMute(String)
     */
    public void forceMuteAll(String sessionId, MuteAllProperties properties) throws OpenTokException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new InvalidArgumentException("Session or Connection string null or empty");
        }
        client.forceMuteAllStream(sessionId, properties);
    }

    /**
     * Disables the active mute state of the session. After you call this method, new streams
     * published to the session will no longer have audio muted.
     * <p>
     * After you call the {@link OpenTok#forceMuteAll OpenTok.forceMuteAll()} method,
     * any streams published after the call are published with audio muted. When you call the
     * <code>OpenTok.disableForceMute()</code> method, future streams published to the session
     * are not muted (but any existing muted streams remain muted).
     *
     * @param sessionId The session ID.
     *
     * @see #forceMuteAll(String, MuteAllProperties)
     */
    public void disableForceMute(String sessionId) throws OpenTokException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new InvalidArgumentException("Session or Connection string null or empty");
        }
        client.disableForceMute(sessionId);
    }

    /**
     * Gets a {@link Stream} object for the given session ID and stream ID.
     *
     * @param sessionId The session ID.
     * @param streamId The stream ID.
     * @return The {@link Stream} object.
     */
    public Stream getStream(String sessionId, String streamId) throws OpenTokException {
        String stream = client.getStream(sessionId, streamId);
        try {
            return streamReader.readValue(stream);
        } catch (Exception e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Gets a list of {@link Stream} objects for the given session ID.
     *
     * @param sessionId The session ID.
     *
     * @return The list of {@link Stream} objects.
     */
    public StreamList listStreams(String sessionId) throws OpenTokException {
        String streams = client.listStreams(sessionId);
        try {
            return streamListReader.readValue(streams);
        } catch (JsonProcessingException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Dials a SIP gateway to connect it an OpenTok session.
     *
     * @param sessionId The session ID.
     *
     * @param token  OpenTok token to be used for the participant being called. You can add token
     * data to identify that the participant is on a SIP endpoint or for other identifying data,
     * such as phone numbers. (The OpenTok client libraries include properties for inspecting
     * the connection data for a client connected to a session.) See the
     * <a href="https://tokbox.com/developer/guides/signaling/">Token Creation developer guide</a>.
     *
     * @param properties The {@link SipProperties} object defining options for the SIP call.
     *
     * @return The {@link Sip} object.
     */
    public Sip dial(String sessionId, String token, SipProperties properties) throws OpenTokException {
        if ((StringUtils.isEmpty(sessionId) || StringUtils.isEmpty(token) || properties == null || StringUtils.isEmpty(properties.sipUri()))) {
            throw new InvalidArgumentException("Session id or token is null or empty or sip properties is null or sip uri empty or null.");
        }
        String sip = client.sipDial(sessionId, token, properties);
        try {
            return sipReader.readValue(sip);
        } catch (JsonProcessingException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Send DTMF digits to all clients in a session.
     *
     * @param sessionId The session ID.
     * @param dtmfDigits The string of DTMF digits to send. This can include 0-9, "*", "#",
     * and "p". A p indicates a pause of 500ms (if you need to add a delay in sending the digits).
     *
     * @throws OpenTokException
     */
    public void playDTMF(String sessionId, String dtmfDigits) throws OpenTokException {
        client.playDtmfAll(sessionId, dtmfDigits);
    }

    /**
     * Send DTMF digits a specific client in a session.
     *
     * @param sessionId The session ID.
     * @param connectionId The session ID of the client to receive the DTMF digits.
     * @param dtmfDigits The string of DTMF digits to send. This can include 0-9, "*", "#",
     * and "p". A p indicates a pause of 500ms (if you need to add a delay in sending the digits).
     *
     * @throws OpenTokException
     */
    public void playDTMF(String sessionId, String connectionId, String dtmfDigits) throws OpenTokException {
        client.playDtmfSingle(sessionId, connectionId, dtmfDigits);
    }

    /**
     * Send audio from a Vonage Video API session to a WebSocket. For more information, see the
     * <a href="https://tokbox.com/developer/guides/audio-connector/">Audio Connector developer guide</a>.
     *
     * @param sessionId The session ID.
     * @param token The OpenTok token to be used for the Audio Connector connection to the
     *              OpenTok session. You can add token data to identify that the connection
     *              is the Audio Connector endpoint or for other identifying data.
     * @param properties The ConnectProperties object defines options used in the request
     *                   to the Audio Connector API endpoint.
     *
     * @return The Audio Connect response object from the server.
     *
     */
    public AudioConnector connectAudioStream(String sessionId, String token, AudioConnectorProperties properties) throws OpenTokException {
        try {
            return connectReader.readValue(client.connectAudioStream(sessionId, token, properties));
        } catch (JsonProcessingException ex) {
            throw new RequestException("Exception mapping json: " + ex.getMessage(), ex);
        }
    }

    /**
     * Starts an Experience Composer render for an OpenTok session. For more information, see the
     * <a href="https://tokbox.com/developer/guides/experience-composer">Experience Composer developer guide</a>.
     *
     * @param sessionId The session ID.
     * @param properties The {@link RenderProperties} object defining the properties for the Render call.
     *
     * @return The {@link Render} response object.
     *
     * @throws OpenTokException
     */
    public Render startRender(String sessionId, String token, RenderProperties properties) throws OpenTokException {
        if (StringUtils.isEmpty(sessionId) || StringUtils.isEmpty(token) || properties == null) {
            throw new InvalidArgumentException("Session id, token and properties are all required.");
        }
        String render = client.startRender(sessionId, token, properties);
        try {
            return renderReader.readValue(render);
        } catch (JsonProcessingException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Gets a Render object, with details on an Experience Composer.
     *
     * @param renderId The ID of the Experience Composer to retrieve.
     *
     * @return The {@link Render} response object associated with the provided ID.
     *
     * @throws OpenTokException
     */
    public Render getRender(String renderId) throws OpenTokException {
        if (StringUtils.isEmpty(renderId)) {
            throw new InvalidArgumentException("Render id is required.");
        }
        String render = client.getRender(renderId);
        try {
            return renderReader.readValue(render);
        } catch (JsonProcessingException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Stops an Experience Composer of an OpenTok session. Note that by default
     * Experience Composers automatically stop 2 hours after they are started. You can also set a different
     * maxDuration value when you create the Experience Composer. When the Experience Composer ends, an event is
     * posted to the callback URL, if you have configured one for the project.
     *
     * @param renderId The ID of the Experience Composer to stop.
     *
     * @throws OpenTokException
     */
    public void stopRender(String renderId) throws OpenTokException {
        if (StringUtils.isEmpty(renderId)) {
            throw new InvalidArgumentException("Render id is required.");
        }
        client.stopRender(renderId);
    }

    /**
     * Gets a list of Render objects, representing Experience Composers associated with the
     * OpenTok project.
     *
     * @return The list of {@link Render} objects.
     *
     * @throws OpenTokException
     */
    public List<Render> listRenders() throws OpenTokException {
        return listRenders(null, null);
    }

    /**
     * Gets a list of Render objects, representing a list of Experience Composers associated
     * with the OpenTok project.
     *
     * @param offset (optional) Start offset in the list of existing Renders.
     * @param count (optional) Number of Renders to retrieve starting at offset. Maximum 1000.
     *
     * @return The list of {@link Render} objects.
     *
     * @throws OpenTokException
     */
    public List<Render> listRenders(Integer offset, Integer count) throws OpenTokException {
        String response = client.listRenders(offset, count);
        try {
            JsonNode root = new ObjectMapper().readTree(response);
            return renderListReader.readValue(root.get("items"));
        } catch (IOException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Use the Live Captions API to transcribe audio streams and generate real-time captions for your application.
     * Live Captions is enabled by default for all projects, and it is a usage-based product. The Live Captions
     * feature is only supported in routed sessions (sessions that use the OpenTok Media Router). You can send up to
     * 50 audio streams from a single Vonage session at a time to the transcription service for captions.
     *
     * @param sessionId The session ID of the OpenTok session. The audio from Publishers publishing into
     * this session will be used to generate the captions.
     *
     * @param token A valid OpenTok token with role set to Moderator.
     *
     * @param properties The {@link CaptionProperties} object defining optional properties of the live captioning.
     *
     * @return A {@link Caption} response containing the captions ID for this call.
     *
     * @throws OpenTokException
     */
    public Caption startCaptions(String sessionId, String token, CaptionProperties properties) throws OpenTokException {
        if (StringUtils.isEmpty(sessionId)) {
            throw new InvalidArgumentException("Session ID is required.");
        }
        if (StringUtils.isEmpty(token)) {
            throw new InvalidArgumentException("Token is required.");
        }
        String captions = client.startCaption(sessionId, token,
                properties != null ? properties : CaptionProperties.Builder().build()
        );
        try {
            return captionReader.readValue(captions);
        } catch (JsonProcessingException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Use this method to stop live captions for a session.
     *
     * @param captionsId The unique ID for the audio captioning session,
     * as obtained from {@linkplain Caption#getCaptionsId()}.
     *
     * @throws OpenTokException
     */
    public void stopCaptions(String captionsId) throws OpenTokException {
        if (StringUtils.isEmpty(captionsId)) {
            throw new InvalidArgumentException("Captions id is required.");
        }
        client.stopCaption(captionsId);
    }

    /**
     * Used to create an OpenTok object with advanced settings. You can set
     * the request timeout for API calls and a proxy to use for API calls.
     * <p>
     * If you do not need to set these advanced settings, you can use the
     * {@link OpenTok OpenTok()} constructor to build the OpenTok object.
     */
    public static class Builder {
        private int apiKey, requestTimeout;
        private String apiSecret, applicationId, apiUrl, appendUserAgent, principal, password;
        private Path privateKeyPath;
        private Proxy proxy;
        private ProxyAuthScheme proxyAuthScheme;

        /**
         * Constructs a new OpenTok.Builder object.
         *
         * @param apiKey The API key for your OpenTok project.
         *
         * @param apiSecret The API secret for your OpenTok project. You can obtain
         * your API key and secret from your <a href="https://tokbox.com/account">Video API account</a>.
         * Do not publicly share your API secret. 
         */
        public Builder(int apiKey, String apiSecret) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
        }

        public Builder(String applicationId, Path privateKeyPath) {
            this.applicationId = UUID.fromString(
                    Objects.requireNonNull(applicationId, "Vonage Application ID is required")
            ).toString();
            this.privateKeyPath = Objects.requireNonNull(privateKeyPath, "Private key path is required.");
        }

        /**
         * Do not use. This method is used by Vonage for testing.
         */
        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        /**
         * Sets a proxy server that the HTTP client will use when making calls to
         * the OpenTok REST API.
         */
        public Builder proxy(Proxy proxy) {
            proxy(proxy, null, null, null);
            return this;
        }

        /**
         * Specify the timeout for HTTP requests (in seconds). The default
         * timeout is 60 seconds.
         */
        public Builder requestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout * 1000;
            return this;
        }

        public Builder proxy(Proxy proxy, ProxyAuthScheme proxyAuthScheme, String principal, String password) {
            this.proxy = proxy;
            this.proxyAuthScheme = proxyAuthScheme;
            this.principal = principal;
            this.password = password;
            return this;
        }

        /**
         * Append a custom string to the client's User-Agent. This is to enable tracking for custom integrations.
         *
         * @param appendUserAgent The string to append to the user agent.
         *
         * @return This Builder with the additional user agent string.
         */
        public Builder appendToUserAgent(String appendUserAgent) {
            this.appendUserAgent = appendUserAgent;
            return this;
        }

        /**
         * Builds the OpenTok object with the settings provided to this
         * Builder object.
         *
         * @return The OpenTok object.
         */
        public OpenTok build() {
            HttpClient.Builder clientBuilder = new HttpClient.Builder(apiKey, apiSecret, applicationId, privateKeyPath);

            if (apiUrl != null) {
                clientBuilder.apiUrl(apiUrl);
            }
            if (proxy != null) {
                clientBuilder.proxy(proxy, proxyAuthScheme, principal, password);
            }
            if (requestTimeout != 0) {
                clientBuilder.requestTimeoutMS(requestTimeout);
            }
            if (appendUserAgent != null && !appendUserAgent.trim().isEmpty()) {
                clientBuilder.userAgent(DefaultUserAgent.DEFAULT_USER_AGENT+" "+appendUserAgent);
            }

            return new OpenTok(apiKey, apiSecret, applicationId, privateKeyPath, clientBuilder.build());
        }
    }

    /**
     * Call this method when you are done using the OpenTok object,
     * to prevent leaked file descriptors.
     */
    public void close() {
        client.close();
    }
}
