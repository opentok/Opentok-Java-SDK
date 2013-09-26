import com.opentok.api.API_Config;
import com.opentok.api.OpenTokSDK;
import com.opentok.api.OpenTokSession;
import com.opentok.api.constants.SessionProperties;
import com.opentok.api.constants.RoleConstants;
import com.opentok.exception.OpenTokException;

class Test {
	public static void main(String argv[]) throws OpenTokException {
		// Set your API key and secret in the API_Config class
		OpenTokSDK sdk = new OpenTokSDK(API_Config.API_KEY,API_Config.API_SECRET);

		//Generate an OpenTok server-enabled session
		System.out.println(sdk.create_session().session_id);
		System.out.println();

		//Generate a peer-to-peer session
        SessionProperties sp = new SessionProperties();
        sp.p2p_preference = "enabled";

		OpenTokSession session = sdk.create_session(null, sp);
        String sessionId = session.getSessionId();
        System.out.println(sessionId);
		System.out.println();

		//Generate a publisher token
		String s = sdk.generate_token(sessionId);
		System.out.println(s);

		//Generate a subscriber token
		System.out.println(sdk.generate_token(sessionId,RoleConstants.SUBSCRIBER));
		System.out.println(s);

		//Generate a moderator token
		System.out.println(sdk.generate_token(sessionId,RoleConstants.MODERATOR));

		// Generate a subscriber token that has connection data....

		// Use the RoleConstants value appropriate for the client.
		String role = RoleConstants.SUBSCRIBER;

		// Replace with meaningful metadata for the client.
		String connectionData = "username=Bob,userLevel=4";
		String token = sdk.generate_token(sessionId, role, null, connectionData);
		System.out.println(token);

	}
}
