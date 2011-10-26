import com.opentok.api.API_Config;
import com.opentok.api.OpenTokSDK;
import com.opentok.api.constants.SessionProperties;
import com.opentok.api.constants.RoleConstants;
import com.opentok.exception.OpenTokException;

class Test {
	public static void main(String argv[]) throws OpenTokException {
		OpenTokSDK sdk = new OpenTokSDK(API_Config.API_KEY,API_Config.API_SECRET);

		//Generate a token
		String s = sdk.generate_token("session");
		System.out.println(s);

		System.out.println(sdk.generate_token("session",RoleConstants.PUBLISHER));
		System.out.println(sdk.generate_token("session",RoleConstants.SUBSCRIBER));
		System.out.println(sdk.generate_token("session",RoleConstants.MODERATOR));



		//Generate a basic session
		System.out.println(sdk.create_session().session_id);

		System.out.println();

		//Generate Session Properties for a session
		SessionProperties sp = new SessionProperties();
		
		//Add SessionProperties here if any are needed (e.g. p2p)

		//Generate a session with a location hint and session properties
		System.out.println(sdk.create_session("127.0.0.1", sp).session_id);
	}
}
