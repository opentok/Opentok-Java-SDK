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
* Contains methods for creating OpenTok sessions and generating tokens.
* <p>
* To create a new OpenTokSDK object, call the OpenTokSDK constructor with your OpenTok API key
* and the API secret from <a href="https://dashboard.tokbox.com">the OpenTok dashboard</a>. Do not publicly share
* your API secret. You will use it with the OpenTokSDK constructor (only on your web
* server) to create OpenTok sessions.
* <p>
* Be sure to include the entire OpenTok server SDK on your web server.
*/
public class OpenTok {

    private int apiKey;
    private String apiSecret;
    protected HttpClient client;

    /**
     * Creates an OpenTokSDK object.
     *
     * @param apiKey Your OpenTok API key. (See the <a href="https://dashboard.tokbox.com">OpenTok dashboard</a>
     * page)
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
     * Creates a token for connecting to an OpenTok session. In order to authenticate a user connecting to an OpenTok session
     * that user must pass an authentication token along with the API key.
     * The following Java code example shows how to obtain a token:
     * <p>
     * <pre>
     * import com.opentok.api.OpenTokSDK;
     *
     * class Test {
     *     public static void main(String argv[]) throws OpenTokException {
     *         int API_KEY = 0; // Replace with your OpenTok API key (see http://dashboard.tokbox.com).
     *         String API_SECRET = ""; // Replace with your OpenTok API secret.
     *         OpenTokSDK sdk = new OpenTokSDK(API_KEY, API_SECRET);
     *
     *         //Generate a basic session. Or you could use an existing session ID.
     *         String sessionId = System.out.println(sdk.createSession());
     *
     *         String token = sdk.generateToken(sessionId);
     *         System.out.println(token);
     *     }
     * }
     * </pre>
     * <p>
     * The following Java code example shows how to obtain a token that has a role of "subscriber" and that has
     * a connection metadata string:
     * <p>
     * <pre>
     * import com.opentok.Role;
     *
     * class Test {
     *     public static void main(String argv[]) throws OpenTokException {
     *         int API_KEY = 0; // Replace with your OpenTok API key (see http://dashboard.tokbox.com).
     *         String API_SECRET = ""; // Replace with your OpenTok API secret.
     *         OpenTokSDK sdk = new OpenTokSDK(API_KEY, API_SECRET);
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
     *         // Generate a token.
     *         String token = sdk.generateToken(sessionId, Role.PUBLISHER, null, connectionMetadata);
     *         System.out.println(token);
     *     }
     * }
     * </pre>
     * <p>
     * For testing, you can also use the <a href="https://dashboard.tokbox.com/projects">OpenTok dashboard</a>
     * page to generate test tokens.
     *
     * @param sessionId The session ID corresponding to the session to which the user will connect.
     *
     * @param role Each role defines a set of permissions granted to the token.
     * Valid values are defined in the Role class:
     *
     *   * `SUBSCRIBER` &mdash; A subscriber can only subscribe to streams.</li>
     *
     *   * `PUBLISHER` &mdash; A publisher can publish streams, subscribe to streams, and signal.
     *     (This is the default value if you do not specify a value for the `role` parameter.)</li>
     *
     *   * `MODERATOR` &mdash; In addition to the privileges granted to a publisher, a moderator
     *     can call the `forceUnpublish()` and `forceDisconnect()` method of the
     *     Session object.</li>
     *
     * @param expireTime The expiration time, in seconds, since the UNIX epoch. Pass in 0 to use
     * the default expiration time of 24 hours after the token creation time. The maximum expiration
     * time is 30 days after the creation time.
     *
     * @param connectionData A string containing metadata describing the end-user. For example, you can pass the
     * user ID, name, or other data describing the end-user. The length of the string is limited to 1000 characters.
     * This data cannot be updated once it is set.
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

    public String generateToken(String sessionId) throws OpenTokException {
        return generateToken(sessionId, new TokenOptions.Builder().build());
    }

    /**
     * Creates a new OpenTok session and returns the session ID, which uniquely identifies the session.
     * <p>
     * For example, when using the OpenTok JavaScript library,
     * use the session ID in JavaScript on the page that you serve to the client. The JavaScript will use this
     * value when calling the <a href="http://tokbox.com/opentok/libraries/client/js/reference/Session.html#connect">connect()</a>
     * method of the Session object (to connect a user to an OpenTok session).
     * <p>
     * OpenTok sessions do not expire. However, authentication tokens do expire (see the
     * {@link #generateToken(String, String, long, String)} method).
     * Also note that sessions cannot explicitly be destroyed.
     * <p>
     * A session ID string can be up to 255 characters long.
     * <p>
     * Calling this method results in an {@link com.opentok.exception.OpenTokException} in the event of an error. Check
     * the error message for details.
     * <p>
     * The following code creates a session that uses the OpenTok Media Router:
     *
     * <pre>
     * import com.opentok.api.OpenTokSDK;
     * import com.opentok.SessionProperties;
     *
     * class Test {
     *     public static void main(String argv[]) throws OpenTokException {
     *         int API_KEY = 0; // Replace with your OpenTok API key.
     *         String API_SECRET = ""; // Replace with your OpenTok API secret.
     *         OpenTokSDK sdk = new OpenTokSDK(API_KEY, API_SECRET);
     *
     *         String sessionId = sdk.createSession();
     *         System.out.println(sessionId);
     *     }
     * }
     * </pre>
     *
     * The following code creates a peer-to-peer session:
     *
     * <pre>
     * import com.opentok.api.OpenTokSDK;
     * import com.opentok.SessionProperties;
     *
     * class Test {
     *     public static void main(String argv[]) throws OpenTokException {
     *         int API_KEY = 0; // Replace with your OpenTok API key.
     *         String API_SECRET = ""; // Replace with your OpenTok API secret.
     *         OpenTokSDK sdk = new OpenTokSDK(API_KEY, API_SECRET);
     *
     *         SessionProperties sp = new SessionProperties();
     *         sp.p2p_preference = "enabled";
     *
     *         String sessionId = sdk.createSession(null, sp);
     *         System.out.println(sessionId);
     *     }
     * }
     * </pre>
     *
     * You can also create a session using the <a href="http://www.tokbox.com/opentok/api/#session_id_production">OpenTok
     * REST API</a> or the <a href="https://dashboard.tokbox.com/projects">OpenTok dashboard</a>.
     *
     * @param properties Defines whether the session's streams will be transmitted directly between peers or
     * using the OpenTok media server. You can set the following possible values:
     * <p>
     * <ul>
     *   <li>
     *     "disabled" (the default) &mdash; The session's streams will all be relayed using the OpenTok media server.
     *     <br><br>
     *     <i>In OpenTok v2:</i> The <a href="http://www.tokbox.com/blog/mantis-next-generation-cloud-technology-for-webrtc/">OpenTok
     *     media server</a> provides benefits not AVAILABLE in peer-to-peer sessions. For example, the OpenTok media server can
     *     decrease bandwidth usage in multiparty sessions. Also, the OpenTok server can improve the quality of the user experience
     *     through <a href="http://www.tokbox.com/blog/quality-of-experience-and-traffic-shaping-the-next-step-with-mantis/">dynamic
     *     traffic shaping</a>. For information on pricing, see the <a href="http://www.tokbox.com/pricing">OpenTok pricing page</a>.
     *     <br><br>
     *   </li>
     *   <li>
     *     "enabled" &mdash; The session will attempt to transmit streams directly between clients.
     *     <br><br>
     *     <i>In OpenTok v1:</i> Peer-to-peer streaming decreases latency and improves quality. If peer-to-peer streaming
     *     fails (either when streams are initially published or during the course of a session), the session falls back to using
     *     the OpenTok media server to relaying streams. (Peer-to-peer streaming uses UDP, which may be blocked by a firewall.)
     *     For a session created with peer-to-peer streaming enabled, only two clients can connect to the session at a time.
     *     If an additional client attempts to connect, the client dispatches an exception event.
     *   </li>
     * </ul>
     *
     * @return A session ID for the new session. For example, when using the OpenTok JavaScript library, use this session ID
     * in JavaScript on the page that you serve to the client. The JavaScript will use this value when calling the
     * <code>connect()</code> method of the Session object (to connect a user to an OpenTok session).
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
     * Creates an OpenTok session and returns the session ID, with the default properties. The
     * session uses the OpenTok media server. And the session uses the first client connecting
     * to determine the location of OpenTok server to use.
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
     * @param offset The index offset of the first archive. 0 is offset of the most recently STARTED archive.
     * 1 is the offset of the archive that STARTED prior to the most recent archive.
     * @param count The number of archives to be returned. The maximum number of archives returned is 1000.
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
     * You can only delete an archive which has a status of "AVAILABLE" or "UPLOADED". Deleting an archive
     * removes its record from the list of archives. For an "AVAILABLE" archive, it also removes the archive
     * file, making it unavailable for download.
     *
     * @param archiveId The archive ID of the archive you want to delete.
     */
    public void deleteArchive(String archiveId) throws OpenTokException {
        this.client.deleteArchive(archiveId);
    }
}
