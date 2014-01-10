# OpenTok Server SDK for Java

The OpenTok server SDKs include code for your web server. Use these SDKs to generate
[sessions](http://tokbox.com/opentok/tutorials/create-session/) and to obtain
[tokens](http://tokbox.com/opentok/tutorials/create-token/) for 
[OpenTok](http://www.tokbox.com/) applications. This version of the SDK also includes
support for working with OpenTok 2.0 archives.

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


# Changes in v2.0 of the OpenTok Java SDK

This version of the SDK includes support for working with OpenTok 2.0 archives. (This API does not work
with OpenTok 1.0 archives.)

The API_Config class has been removed. Store your OpenTok API key and API secret in code outside
of the SDK files.

The create_session() method has been renamed createSession(). Also, the method has changed to
take one parameter: a SessionProperties object. You now generate a SessionProperties object using a
Builder pattern. And the createSession() method returns a session ID string, not a Session object.
(The Session class has been removed.)

The generate_token() method has been renamed generateToken().

# Creating Sessions
Use the `createSession()` method of the OpenTokSDK object to create a session and a session ID.

The following code creates an OpenTok server-enabled session:

<pre>
import com.opentok.api.OpenTokSDK;
import com.opentok.exception.OpenTokException;

class Test {
    public static void main(String argv[]) throws OpenTokException {
        int API_KEY = 0; // Replace with your OpenTok API key.
        String API_SECRET = ""; // Replace with your OpenTok API secret.
        OpenTokSDK sdk = new OpenTokSDK(API_KEY, API_SECRET);

        //Generate an OpenTok server-enabled session
        System.out.println(sdk.createSession());
   }
}
</pre>

The following code creates a peer-to-peer session:

<pre>
import com.opentok.api.OpenTokSDK;
import com.opentok.api.constants.SessionProperties;
import com.opentok.exception.OpenTokException;

class Test {
    public static void main(String argv[]) throws OpenTokException {
        int API_KEY = 0; // Replace with your OpenTok API key.
        String API_SECRET = ""; // Replace with your OpenTok API secret.

        OpenTokSDK sdk = new OpenTokSDK(API_KEY, API_SECRET);

        SessionProperties sp = new SessionProperties.Builder().p2pPreference(true).build();
        String sessionId = sdk.create_session(sp);
        System.out.println(sessionId);
    }
}
</pre>

# Generating tokens
Use the  `generate_token()` method of the OpenTokSDK object to create an OpenTok token:

The following example shows how to obtain a token:

<pre>
import com.opentok.api.OpenTokSDK;
import com.opentok.exception.OpenTokException;

class Test {
    public static void main(String argv[]) throws OpenTokException {
        // Set the following constants with the API key and API secret
        // that you receive when you sign up to use the OpenTok API:
        OpenTokSDK sdk = new OpenTokSDK(API_Config.API_KEY, API_Config.API_SECRET);

        //Generate a basic session. Or you could use an existing session ID.
        String sessionId = System.out.println(sdk.create_session());

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
}
</pre>


# Working with OpenTok 2.0 archives

The following method starts recording an archive of an OpenTok 2.0 session (given a session ID)
and returns the archive ID (on success).

<pre>
java.util.UUID startArchive(OpenTokSDK sdk, String sessionId, String name) {
    try {
        Archive archive = sdk.startArchive(sessionId, name);
        return archive.getId();
    } catch (OpenTokException exception){
        System.out.println(exception.toString());
        return null;
    }
}
</pre>

The following method stops the recording of an archive (given an archive ID), returning
true on success, and false on failure.

<pre>
boolean stopArchive(OpenTokSDK sdk, String archiveId) {
    try {
        Archive archive = sdk.stopArchive(archiveId);
        return true;
    } catch (OpenTokException exception){
        System.out.println(exception.toString());
        return false;
    }
}
</pre>

The following method logs information on a given archive.

<pre>
void logArchiveInfo(OpenTokSDK sdk, String archiveId) {
    try {
        Archive archive = sdk.getArchive(archiveId);
        System.out.println(archive.toString());
    } catch (OpenTokException exception){
        System.out.println(exception.toString());
    }
}
</pre>

The following method logs information on all archives (up to 50)
for your API key:

<pre>
void listArchives(OpenTokSDK sdk) {
    try {
        List<Archive> archives = sdk.listArchives();
        for (int i = 0; i &lt; archives.size(); i++) {
            Archive archive = archives.get(i);
            System.out.println(archive.toString());
        }
    } catch (OpenTokException exception) {
        System.out.println(exception.toString());
    }
}
</pre>



# More information

For details on the API, see the comments in Java files in src/main/java/com/opentok.

For more information on OpenTok, go to <http://www.tokbox.com/>.
