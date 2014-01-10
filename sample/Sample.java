import com.opentok.api.OpenTokSDK;
import com.opentok.api.constants.SessionProperties;
import com.opentok.api.constants.RoleConstants;
import com.opentok.exception.OpenTokException;

class Sample {
    public static void main(String argv[]) throws OpenTokException {
        int API_KEY = 0; // Replace with your OpenTok API key.
        String API_SECRET = ""; // Replace with your OpenTok API secret.
        OpenTokSDK sdk = new OpenTokSDK(API_KEY, API_SECRET);

        // Generate an OpenTok server-enabled session
        System.out.println(sdk.createSession());
        System.out.println();

        // Generate a peer-to-peer session
        SessionProperties sp = new SessionProperties.Builder().p2pPreference(true).build();

        String sessionId = sdk.createSession(sp);
        System.out.println(sessionId);
        System.out.println();

        // Generate a publisher token
        String s = sdk.generateToken(sessionId);
        System.out.println(s);

        // Generate a subscriber token
        System.out.println(sdk.generateToken(sessionId, RoleConstants.SUBSCRIBER));
        System.out.println(s);

        // Generate a moderator token
        System.out.println(sdk.generateToken(sessionId, RoleConstants.MODERATOR));

        // Generate a subscriber token that has connection data....

        // Use the RoleConstants value appropriate for the client.
        String role = RoleConstants.SUBSCRIBER;
        // Replace with meaningful metadata for the client.
        String connectionData = "username=Bob,userLevel=4";
        String token = sdk.generateToken(sessionId, role, 0, connectionData);
        System.out.println(token);
    }
}
