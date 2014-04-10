import com.opentok.api.OpenTok;
import com.opentok.api.Session;
import com.opentok.api.constants.SessionProperties;
import com.opentok.api.constants.RoleConstants;
import com.opentok.exception.OpenTokException;

class Sample {
    public static void main(String argv[]) throws OpenTokException {
        int API_KEY = 0; // Replace with your OpenTok API key (see http://dashboard.tokbox.com).
        String API_SECRET = ""; // Replace with your OpenTok API secret.
        OpenTok sdk = new OpenTok(API_KEY, API_SECRET);

        // Generate a session that uses the OpenTok Media Router
        System.out.println(sdk.createSession().getSessionId());
        System.out.println();

        // Generate a peer-to-peer session
        SessionProperties sp = new SessionProperties.Builder().p2p(true).build();

        Session sessionId = sdk.createSession(sp);
        System.out.println(sessionId);
        System.out.println();

        // Generate a publisher token
        String s = sessionId.generateToken();
        System.out.println(s);

        // Generate a subscriber token
        System.out.println(sessionId.generateToken(RoleConstants.SUBSCRIBER));
        System.out.println(s);

        // Generate a moderator token
        System.out.println(sessionId.generateToken(RoleConstants.MODERATOR));

        // Generate a subscriber token that has connection data....

        // Use the RoleConstants value appropriate for the client.
        String role = RoleConstants.SUBSCRIBER;
        // Replace with meaningful metadata for the client.
        String connectionData = "username=Bob,userLevel=4";
        String token = sessionId.generateToken(role, 0, connectionData);
        System.out.println(token);
    }
}
