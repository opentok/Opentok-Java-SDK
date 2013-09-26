# OpenTok Server SDK for Java

The OpenTok server SDKs include code for your web server. Use these SDKs to generate
[sessions](http://tokbox.com/opentok/tutorials/create-session/) and to obtain
[tokens](http://tokbox.com/opentok/tutorials/create-token/) for 
[OpenTok](http://www.tokbox.com/) applications.

## Download

Download the Java files:

<https://github.com/opentok/Opentok-Java-SDK/archive/master.zip>

## Installing the SDK via Maven

The OpenTok Server SDK for Java defines the class in the com.opentok.api package. If you use Maven, add
the following dependency information to the Project Object Model (pom.xml) file:

<pre>
&lt;dependency&gt;
    &lt;groupId&gt;com.opentok.api&lt;/groupId&gt;
    &lt;artifactId&gt;opentok-java-sdk&lt;/artifactId&gt;
    &lt;version&gt;[0.91.54,)&lt;/version&gt;
&lt;/dependency&gt;
</pre>


## Requirements

The OpenTok Java SDK requires Java 6 or greater.

You need an OpenTok API key and API secret, which you can obtain at <https://dashboard.tokbox.com>.

# API_Config

Replace these two values in the API_Config.java file with your OpenTok API key and API secret.

    public static final int API_KEY = 0;
    public static final String API_SECRET = "";

# Creating Sessions
Use the `createSession()` method of the OpenTokSDK object to create a session and a session ID.

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

# Generating tokens
Use the  `generate_token()` method of the OpenTokSDK object to create an OpenTok token:

The following Java code example shows how to obtain a token:

<pre>
import com.opentok.api.API_Config;
import com.opentok.api.OpenTokSDK;
import com.opentok.exception.OpenTokException;

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

        // Use the RoleConstants value appropriate for the user.
        String role = RoleConstants.SUBSCRIBER;

        // Replace with meaningful metadata for the connection.
        String connectionData = "username=Bob,userLevel=4";

        // Generate a token.
        String token = sdk.generate_token(sessionId, role, null, connectionData);
        System.out.println(token);
    }
}</pre>

# More information

See the [reference documentation](docs/reference.md).

For more information on OpenTok, go to <http://www.tokbox.com/>.
