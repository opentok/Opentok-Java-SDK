# OpenTokSDK Java SDK reference

The OpenTok Server SDK for Java defines an OpenTokSDK class, which needs to be instantiated as an OpenTokSDK object before calling any of its methods.

To create a new OpenTokSDK object, call the OpenTokSDK constructor with the API key 
and the API secret from <a href="https://dashboard.tokbox.com/users/sign_in">your OpenTok dashboard</a>. Do not publicly share 
your API secret. You will use it with the OpenTokSDK constructor (only on your web
server) to create OpenTok sessions.

Be sure to include the entire OpenTok SDK (for a specific language) on your web server. 
In addition to the file defining the OpenTokSDK class, some of the libraries include other required files.
Specifically, the OpenTok Server SDK for Java includes an API_Config.java file (among others).

## create_session() method

The `create_session()` method of the OpenTokSDK object creates a new OpenTok
session from which you can obtain a session ID.

The `create_session()` method has the following parameters:

* location (String) &mdash; An IP address that TokBox will use to situate the session in its global network.

  In general, do not pass in a location hint (or pass in a null value); if no value (or a null value) is passed in,
the session uses a media server based on the location of the first client connecting to the session. Pass a
location hint in only if you know the general geographic region (and a representative IP address) and you think the
first client connecting may not be in that region.

* `properties` (Object) &mdash; Optional. An object used to define
peer-to-peer preferences for the session. The `properties` option includes one property &mdash;
`p2p.preference` (a string). This property determines whether the session's streams will
be transmitted directly between peers. You can set the following possible values:

  * "disabled" (the default) &mdash; The session's streams will all be relayed using the OpenTok media server.
    <br><br>
    **In OpenTok v2:** The <a href="http://www.tokbox.com/blog/mantis-next-generation-cloud-technology-for-webrtc/">OpenTok
media server</a> provides benefits not available in peer-to-peer sessions. For example, the OpenTok media server can
decrease bandwidth usage in multiparty sessions. Also, the OpenTok server can improve the quality of the user experience
through <a href="http://www.tokbox.com/blog/quality-of-experience-and-traffic-shaping-the-next-step-with-mantis/">dynamic
traffic shaping</a>. For information on pricing, see the <a href="http://www.tokbox.com/pricing">OpenTok pricing page</a>.

  * "enabled" &mdash; The session will attempt to transmit streams directly between clients.
    <br><br>
    **In OpenTok v1:** Peer-to-peer streaming decreases latency and improves quality. If peer-to-peer streaming
fails (either when streams are initially published or during the course of a session), the session falls back to using
the OpenTok media server to relaying streams. (Peer-to-peer streaming uses UDP, which may be blocked by a firewall.)
For a session created with peer-to-peer streaming enabled, only two clients can connect to the session at a time.
If an additional client attempts to connect, the TB object on the client dispatches an exception event.


The `create_session` method returns an OpenTokSession object. This
object includes a `getSessionId()` method, which returns the session ID for the
new session. For example, when using the OpenTok JavaScript library, use this 
session ID in JavaScript on the page that you serve to the client.
The JavaScript will use this value when calling the `connect()`
method of the Session object (to connect a user to an OpenTok session).

OpenTok sessions do not expire. However, authentication tokens do expire (see the next section on the
`generate_token()` method.) Also note that sessions cannot explicitly be destroyed.

A session ID string can be up to 255 characters long.

Calling the `create_session()` method results in an `OpenTokException`
in the event of an error. Check the error message for details.

The following code creates an OpenTok server-enabled session:

<pre>
import com.opentok.api.API_Config;
import com.opentok.api.OpenTokSDK;
import com.opentok.api.constants.SessionProperties;

class Test {
    public static void main(String argv[]) throws OpenTokException {
        OpenTokSDK sdk = new OpenTokSDK(API_Config.API_KEY, API_Config.API_SECRET);

        String sessionId = sdk.create_session().getSessionId();
        System.out.println(sessionId);
    }
}
</pre>

The following code creates a peer-to-peer session:

<pre>
import com.opentok.api.API_Config;
import com.opentok.api.OpenTokSDK;
import com.opentok.api.constants.SessionProperties;

class Test {
    public static void main(String argv[]) throws OpenTokException {
        OpenTokSDK sdk = new OpenTokSDK(API_Config.API_KEY, API_Config.API_SECRET);

        SessionProperties sp = new SessionProperties();
        sp.p2p_preference = "enabled";

        String sessionId = sdk.create_session(null, sp).getSessionId();
        System.out.println(sessionId);
    }
}
</pre>

You can also create a session using the <a href="http://www.tokbox.com/opentok/api/#session_id_production">OpenTok
REST API</a> or the <a href="https://dashboard.tokbox.com/projects">OpenTok dashboard</a>.


## generate_token() method

In order to authenticate a user connecting to an OpenTok session, that user must pass an authentication token along with the API key.

The method has the following parameters: 

* session_id (String) &mdash; The session ID corresponding to the session to which the user will connect.

* role (String) &mdash; Optional. Each role defines a set of permissions granted to the token. 
Valid values are defined in the RoleConstants class:

  * `SUBSCRIBER` &mdash; A subscriber can only subscribe to streams.</li>
    
  * `PUBLISHER` &mdash; A publisher can publish streams, subscribe to streams, and signal.
    (This is the default value if you do not specify a value for the `role` parameter.)</li>
    
  * `MODERATOR` &mdash; In addition to the privileges granted to a publisher, a moderator
    can call the `forceUnpublish()` and `forceDisconnect()` method of the 
    Session object.</li>

* expire_time (int) &mdash; Optional. The time when the token
will expire, defined as an integer value for a Unix timestamp (in seconds).
If you do not specify this value, tokens expire 24 hours after being created.
The `expiration_time` value, if specified, must be within 30 days
of the creation time.

* connection_data (String) &mdash; Optional. A string containing metadata describing the end-user. 
For example, you can pass the user ID, name, or other data describing the end-user.
The length of the string is limited to 1000 characters. This data cannot be updated once it is set.

Calling the `generate_token()` method returns a string.

The following Java code example shows how to obtain a token:

<pre>
import com.opentok.api.API_Config;
import com.opentok.api.OpenTokSDK;

class Test {
    public static void main(String argv[]) throws OpenTokException {
        // Set the following constants with the API key and API secret
        // that you receive when you sign up to use the OpenTok API:
        OpenTokSDK sdk = new OpenTokSDK(API_Config.API_KEY, API_Config.API_SECRET);

        //Generate a basic session. Or you could use an existing session ID.
        String sessionId = System.out.println(sdk.create_session().getSessionId());

        String token = sdk.generate_token(sessionId);
        System.out.println(token);
    }
}
</pre>

The following Java code example shows how to obtain a token that has a role of "subscriber" and that has
a connection metadata string:

<pre>import com.opentok.api.API_Config;
import com.opentok.api.OpenTokSDK;
import com.opentok.api.constants.RoleConstants;

class Test {
    public static void main(String argv[]) throws OpenTokException {
        // Set the following constants with the API key and API secret
        // that you receive when you sign up to use the OpenTok API:
        OpenTokSDK sdk = new OpenTokSDK(API_Config.API_KEY, API_Config.API_SECRET);

        //Generate a basic session. Or you could use an existing session ID.
        String sessionId = System.out.println(sdk.create_session().getSessionId());

        // Replace with meaningful metadata for the connection.
        String connectionMetadata = "username=Bob,userLevel=4";

        // Use the RoleConstants value appropriate for the user.
        String role = RoleConstants.SUBSCRIBER;

        // Generate a token.
        String token = sdk.generate_token(sessionId, RoleConstants.PUBLISHER, null, connectionMetadata);
        System.out.println(token);
    }
}</pre>

For testing, you can also use the <a href="https://dashboard.tokbox.com/projects">OpenTok dashboard</a>
page to generate test tokens.
