package com.opentok.api.constants;

import java.util.HashMap;
import java.util.Map;


/**
 * Defines values for the <code>properties</code> parameter of the {@link com.opentok.api.OpenTok#createSession(SessionProperties)} method.
 *
 * @see <a href="../OpenTokSDK.html#createSession(com.opentok.api.constants.SessionProperties)">OpenTokSDK.createSession(SessionProperties)</a>
 */
public class SessionProperties {


    private String location = null;
    private boolean p2p = false;

    private SessionProperties(Builder builder)
    {
        this.location = builder.location;
        this.p2p = builder.p2p;
    }

    public static class Builder
    {
        private String location = null;
        private boolean p2p = false;
        

        public Builder location(String location)
        {
            this.location = location;
            return this;
        }

        public Builder p2p(boolean p2p)
        {
            this.p2p = p2p;
            return this;
        }

        public SessionProperties build()
        {
            return new SessionProperties(this);
        }
    }
    /**
    * An IP address that the OpenTok servers will use to situate the session in its global network. If you
    * do not pass in a location hint, the OpenTok servers will be based on first client connecting to the session.
    */
    public String getLocation() {
        return location;
    }
    
	/**
	 * Defines wether the session's streams will be transmitted directly between peers or using the OpenTok media server:
	 * <p>
	 * <ul>
	 *   <li>
	 *     <code>false</code> &mdash; The session's streams will all be relayed using the OpenTok media server.
	 *     <br><br>
	 *     The OpenTok media server provides benefits not available in peer-to-peer sessions. For example, the OpenTok media server can
	 *     decrease bandwidth usage in multiparty sessions. Also, the OpenTok server can improve the quality of the user experience
	 *     through <a href="http://www.tokbox.com/blog/quality-of-experience-and-traffic-shaping-the-next-step-with-mantis/">dynamic
	 *     traffic shaping</a>. For information on pricing, see the <a href="http://www.tokbox.com/pricing">OpenTok pricing page</a>.
	 *     <br><br>
	 *   </li>
	 *   <li>
	 *     <code>true</code> &mdash; The session will transmit streams directly between clients.
	 *   </li>
	 * </ul>
	 */
    public boolean isP2p() {
        return p2p;
    }

    public Map<String, String> toMap() {
        Map<String, String> params = new HashMap<String, String>();
        if (null != location) {
            params.put("location", location);
        }
        if (p2p) {
            params.put("p2p.preference", "enabled");
        }
        return params;
    }

};