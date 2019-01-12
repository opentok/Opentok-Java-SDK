/**
 * OpenTok Java SDK
 * Copyright (C) 2019 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
* Contains methods for creating OpenTok sessions, generating tokens, and working with archives.
* <p>
* To create a new OpenTok object, call the OpenTok constructor with your OpenTok API key
* and the API secret for your <a href="https://tokbox.com/account">TokBox account</a>. Do not publicly share
* your API secret. You will use it with the OpenTok constructor (only on your web
* server) to create OpenTok sessions.
* <p>
* Be sure to include the entire OpenTok server SDK on your web server.
*/
public class OpenTok {

    private int apiKey;
    private String apiSecret;
    protected HttpClient client;
    static protected ObjectReader archiveReader = new ObjectMapper()
            .readerFor(Archive.class);
    static protected ObjectReader archiveListReader = new ObjectMapper()
            .readerFor(ArchiveList.class);
    static protected ObjectReader createdSessionReader = new ObjectMapper()
            .readerFor(CreatedSession[].class);
    static protected ObjectReader streamReader = new ObjectMapper()
            .readerFor(Stream.class);
    static protected ObjectReader streamListReader = new ObjectMapper()
            .readerFor(StreamList.class);
    static protected ObjectReader sipReader = new ObjectMapper()
            .readerFor(Sip.class);
    static protected ObjectReader broadcastReader = new ObjectMapper()
            .readerFor(Broadcast.class);
    static final String defaultApiUrl = "https://api.opentok.com";

    /**
     * Creates an OpenTok object.
     *
     * @param apiKey Your OpenTok API key. (See your <a href="https://tokbox.com/account">TokBox account page</a>.)
     * @param apiSecret Your OpenTok API secret. (See your <a href="https://tokbox.com/account">TokBox account page</a>.)
     */
    public OpenTok(int apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret.trim();
        this.client = new HttpClient.Builder(apiKey, apiSecret).build();
    }
    
    private OpenTok(int apiKey, String apiSecret, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret.trim();
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
     *     public static void main(String argv[]) throws OpenTokException {
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
     * For testing, you can also generate tokens by logging in to your <a href="https://tokbox.com/account">TokBox account</a>.
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
        List<String> sessionIdParts = null;
        if (sessionId == null || sessionId == "") {
            throw new InvalidArgumentException("Session not valid");
        }

        try {
            sessionIdParts = Crypto.decodeSessionId(sessionId);
        } catch (UnsupportedEncodingException e) {
            throw new InvalidArgumentException("Session ID was not valid");
        }
        if (!sessionIdParts.contains(Integer.toString(this.apiKey))) {
            throw new InvalidArgumentException("Session ID was not valid");
        }

        // NOTE: kind of wasteful of a Session instance
        Session session = new Session(sessionId, apiKey, apiSecret);
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
     * <a href="https://tokbox.com/account">TokBox account</a>.
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
        final Map<String, Collection<String>> params = _properties.toMap();
        final String response = this.client.createSession(params);

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

    public void signal(String sessionId, SignalProperties props) throws OpenTokException , RequestException, InvalidArgumentException {

        if (sessionId == null || sessionId.isEmpty() ) {
            throw new InvalidArgumentException("Session string null or empty");
        }
        try {
            this.client.signal(sessionId,null,props);

        } catch (Exception e)
        {
            throw e;
        }

    }

    public void signal(String sessionId, String connectionId, SignalProperties props) throws OpenTokException , RequestException , InvalidArgumentException {

        if (sessionId == null || sessionId.isEmpty() || connectionId == null || connectionId.isEmpty()) {
            throw new InvalidArgumentException("Session or Connection string null or empty");
        }
        try {
            this.client.signal(sessionId, connectionId, props);

        } catch (Exception e)
        {
            throw e;
        }

    }
    /**
     * Gets an {@link Archive} object for the given archive ID.
     *
     * @param archiveId The archive ID.
     * @return The {@link Archive} object.
     */
    public Archive getArchive(String archiveId) throws OpenTokException {
        String archive = this.client.getArchive(archiveId);
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
        if (sessionId == null || sessionId.isEmpty() ) {
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
        String archives = this.client.getArchives(sessionId, offset, count);
        try {
            return archiveListReader.readValue(archives);
        } catch (JsonProcessingException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        } catch (IOException e) {
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
        if (sessionId == null || sessionId == "") {
            throw new InvalidArgumentException("Session not valid");
        }
        Boolean hasResolution =  properties != null && properties.resolution() != null && !properties.resolution().isEmpty();
        if(properties != null && properties.outputMode().equals(Archive.OutputMode.INDIVIDUAL) && hasResolution) {
            throw new InvalidArgumentException("The resolution cannot be specified for individual output mode.");
        }
        // TODO: do validation on sessionId and name
        String archive = this.client.startArchive(sessionId, properties);
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

        String archive = this.client.stopArchive(archiveId);
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
        this.client.deleteArchive(archiveId);
    }
    /**
     * Sets the layout type for a composed archive. For a description of layout types, see
     *  <a href="https://tokbox.com/developer/guides/archiving/layout-control.html">Customizing
     *  the video layout for composed archives</a>.
     *
     * @param archiveId {String} The archive ID.
     *
     * @param properties This ArchiveProperties object defining the arachive layout.
     */
    public void setArchiveLayout(String archiveId, ArchiveProperties properties) throws OpenTokException {
        if (StringUtils.isEmpty(archiveId) || properties == null) {
            throw new InvalidArgumentException("ArchiveId is not valid or properties are null");
        }
        this.client.setArchiveLayout(archiveId, properties);
    }
    /**
     * Use this method to start a live streaming for an OpenTok session.
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
     * @param properties This BroadcastProperties object defines options for the broadcast.
     *
     * @return The Broadcast object. This object includes properties defining the broadcast,
     * including the broadcast ID.
     */
    public Broadcast startBroadcast(String sessionId, BroadcastProperties properties) throws OpenTokException {
        if (StringUtils.isEmpty(sessionId) || (properties ==  null)) {
            throw new InvalidArgumentException("Session not valid or broadcast properties is null");
        }

        String broadcast = this.client.startBroadcast(sessionId, properties);
        try {
            return broadcastReader.readValue(
                    broadcast);
        } catch (Exception e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Use this method to stop a live broadcast of an OpenTok session.
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
        String broadcast = this.client.stopBroadcast(broadcastId);
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
        if(StringUtils.isEmpty(broadcastId)) {
            throw new InvalidArgumentException("Broadcast id is null or empty");
        }
        String stream = this.client.getBroadcast(broadcastId);
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
        this.client.setBroadcastLayout(broadcastId, properties);
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
        this.client.setStreamLayouts(sessionId, properties);
    }

    /**
     * Disconnect a client from an OpenTok session
     * <p>
     * Use this API to forcibly terminate a connection of a session.
     *
     * @param sessionId The session ID of the connection
     * @param  connectionId The connection ID to disconnect
     */
    public void forceDisconnect(String sessionId, String connectionId) throws OpenTokException , InvalidArgumentException, RequestException {
        if (sessionId == null || sessionId.isEmpty() || connectionId == null || connectionId.isEmpty()) {
            throw new InvalidArgumentException("Session or Connection string null or empty");
        }
        try {
            this.client.forceDisconnect(sessionId, connectionId);

        } catch (Exception e)
        {
            throw e;
        }
    }


    /**
     * Gets an {@link Stream} object for the given sessionId and streamId.
     *
     * @param sessionId The session ID.
     * @param streamId The stream ID.
     * @return The {@link Stream} object.
     */
    public Stream getStream(String sessionId, String streamId) throws OpenTokException {
        String stream = this.client.getStream(sessionId, streamId);
        try {
            return streamReader.readValue(stream);
        } catch (Exception e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Gets a list of {@link Stream} object for the given session ID.
     *
     * @param sessionId The session ID.
     *
     * @return The list of {@link Stream} objects.
     */
    public StreamList listStreams(String sessionId) throws OpenTokException {
        String streams = this.client.listStreams(sessionId);
        try {
            return streamListReader.readValue(streams);
        } catch (JsonProcessingException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        } catch (IOException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Gets a list of {@link Stream} object for the given session ID.
     *
     * @param sessionId The session ID.
     * @param token The token.
     * @param properties The SipProperties.
     * @return The  {@link Sip} object.
     */
    public Sip dial(String sessionId, String token, SipProperties properties) throws OpenTokException {
        if((StringUtils.isEmpty(sessionId) || StringUtils.isEmpty(token) || properties == null || StringUtils.isEmpty(properties.sipUri()))) {
            throw  new InvalidArgumentException ("Session id or token is null or empty or sip properties is null or sip uri empty or null.");
        }
        String sip = this.client.sipDial(sessionId,token,properties);
        try {
            return sipReader.readValue(sip);
        } catch (JsonProcessingException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        } catch (IOException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }
    public static class Builder {
        private int apiKey;
        private String apiSecret;
        private String apiUrl;
        private Proxy proxy;
        private ProxyAuthScheme proxyAuthScheme;
        private String principal;
        private String password;
        
        public Builder(int apiKey, String apiSecret) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
        }
        
        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }
        
        public Builder proxy(Proxy proxy) {
            proxy(proxy, null, null, null);
            return this;
        }
        
        public Builder proxy(Proxy proxy, ProxyAuthScheme proxyAuthScheme, String principal, String password) {
            this.proxy = proxy;
            this.proxyAuthScheme = proxyAuthScheme;
            this.principal = principal;
            this.password = password;
            return this;
        }
        
        public OpenTok build() {
            HttpClient.Builder clientBuilder = new HttpClient.Builder(apiKey, apiSecret);
            
            if (this.apiUrl != null) {
                clientBuilder.apiUrl(this.apiUrl);
            }
            if (this.proxy != null) {
                clientBuilder.proxy(this.proxy, proxyAuthScheme, principal, password);
            }
            
            return new OpenTok(this.apiKey, this.apiSecret, clientBuilder.build());
        }
    }

    public void close() {
        this.client.close();
    }
}
