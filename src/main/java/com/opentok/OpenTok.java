package com.opentok;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.InvalidArgumentException;
import com.opentok.exception.RequestException;
import com.opentok.util.Crypto;
import com.opentok.util.HttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xml.sax.InputSource;

/**
* Contains methods for creating OpenTok sessions, generating tokens, and working with archives.
* <p>
* To create a new OpenTok object, call the OpenTok constructor with your OpenTok API key
* and the API secret from <a href="https://dashboard.tokbox.com">the OpenTok dashboard</a>. Do not publicly share
* your API secret. You will use it with the OpenTok constructor (only on your web
* server) to create OpenTok sessions.
* <p>
* Be sure to include the entire OpenTok server SDK on your web server.
*/
public class OpenTok {

    private int apiKey;
    private String apiSecret;
    protected HttpClient client;

    /**
     * Creates an OpenTok object.
     *
     * @param apiKey Your OpenTok API key. (See the <a href="https://dashboard.tokbox.com">OpenTok
     * dashboard</a> page.)
     * @param apiSecret Your OpenTok API secret. (See the <a href="https://dashboard.tokbox.com">OpenTok dashboard</a>
     * page)
     */
    public OpenTok(int apiKey, String apiSecret) {
        this(apiKey, apiSecret, "https://api.opentok.com");
    }

    public OpenTok(int apiKey, String apiSecret, String apiUrl) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret.trim();
        this.client = new HttpClient.Builder(apiKey, apiSecret)
                .apiUrl(apiUrl)
                .build();
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
     *         int API_KEY = 0; // Replace with your OpenTok API key (see http://dashboard.tokbox.com).
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
     * For testing, you can also use the <a href="https://dashboard.tokbox.com/projects">OpenTok
     * dashboard</a> page to generate test tokens.
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
        if(sessionId == null || sessionId == "") {
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
     *         int API_KEY = 0; // Replace with your OpenTok API key (see http://dashboard.tokbox.com).
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
     * Creates a new OpenTok session and returns the session ID, which uniquely identifies
     * the session.
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
     * The following code creates a peer-to-peer session:
     *
     * <pre>
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
     *         SessionProperties sp = new SessionProperties().Builder().p2p(true).build();
     *
     *         Session session = sdk.createSession(sp);
     *         System.out.println(session.getSessionId());
     *     }
     * }
     * </pre>
     *
     * You can also create a session using the <a href="http://www.tokbox.com/opentok/api/#session_id_production">OpenTok
     * REST API</a> or the <a href="https://dashboard.tokbox.com/projects">OpenTok dashboard</a>.
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
     * @return A session ID for the new session. For example, when using the OpenTok.js library, use
     * this session ID when calling the <code>OT.initSession()</code> method.
     */
    public Session createSession(SessionProperties properties) throws OpenTokException {
        Map<String, Collection<String>> params;
        String xpathQuery = "/sessions/Session/session_id";

        // NOTE: doing this null check twice is kind of ugly
        if(properties != null) {
            params = properties.toMap();
        } else {
            params = null;
        }
        
        String xmlResponse = this.client.createSession(params);


        // NOTE: doing this null check twice is kind of ugly
        try {
            if (properties != null) {
                return new Session(readXml(xpathQuery, xmlResponse), apiKey, apiSecret, properties);
            } else {
                return new Session(readXml(xpathQuery, xmlResponse), apiKey, apiSecret);
            }
        } catch (XPathExpressionException e) {
            throw new OpenTokException("Cannot create session. Could not read the response: " + xmlResponse);
        }
    }

    /**
     * Creates an OpenTok session with the default settings:
     *
     * <ul>
     *     <li>The session uses the OpenTok media server.
     *     <li>The session uses the first client connecting to determine the location of the
     *     OpenTok server to use.</li>
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
     * @see #createSession(SessionProperties)
     */
    public Session createSession() throws OpenTokException {
        return createSession(null);
    }

    private static String readXml(String xpathQuery, String xml) throws XPathExpressionException {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        InputSource source = new InputSource(new StringReader(xml));
        return xpath.evaluate(xpathQuery, source);
    }
    
    /**
     * Gets an {@link Archive} object for the given archive ID.
     *
     * @param archiveId The archive ID.
     * @return The {@link Archive} object.
     */
    public Archive getArchive(String archiveId) throws OpenTokException {
        ObjectMapper mapper = new ObjectMapper();
        String archive = this.client.getArchive(archiveId);
        try {
            return mapper.readValue(archive, Archive.class);
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
    public List<Archive> listArchives() throws OpenTokException {
        return listArchives(0, 1000);
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
    public List<Archive> listArchives(int offset, int count) throws OpenTokException {
        ObjectMapper mapper = new ObjectMapper();
        String archive = this.client.getArchives(offset, count);
        try {
            JsonParser jp = mapper.getFactory().createParser(archive);
            return mapper.readValue(mapper.treeAsTokens(mapper.readTree(jp).get("items")),
                    TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Archive.class));

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (JsonMappingException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        } catch (JsonParseException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        } catch (IOException e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }
    
    /**
     * Starts archiving an OpenTok 2.0 session.
     *
     * <p>
     * Clients must be actively connected to the OpenTok session for you to successfully start recording an archive.
     * <p>
     * You can only record one archive at a time for a given session. You can only record archives
     * of sessions that uses the OpenTok Media Router; you cannot archive peer-to-peer sessions.
     *
     * @param sessionId The session ID of the OpenTok session to archive.
     * @param name The name of the archive. You can use this name to identify the archive. It is a property
     * of the Archive object, and it is a property of archive-related events in the OpenTok JavaScript SDK.
     *
     * @return The Archive object. This object includes properties defining the archive, including the archive ID.
     */
    public Archive startArchive(String sessionId, String name) throws OpenTokException {
        if (sessionId == null || sessionId == "") {
            throw new InvalidArgumentException("Session not valid");
        }
        // TODO: do validation on sessionId and name
        String archive = this.client.startArchive(sessionId, name);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(archive, Archive.class);
        } catch (Exception e) {
            throw new RequestException("Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Stops an OpenTok archive that is being recorded.
     * <p>
     * Archives automatically stop recording after 90 minutes or when all clients have disconnected from the
     * session being archived.
     *
     * @param archiveId The archive ID of the archive you want to stop recording.
     * @return The Archive object corresponding to the archive being STOPPED.
     */
    public Archive stopArchive(String archiveId) throws OpenTokException {

        String archive = this.client.stopArchive(archiveId);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(archive, Archive.class);
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
}
