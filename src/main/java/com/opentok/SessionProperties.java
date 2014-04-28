package com.opentok;

import com.opentok.exception.InvalidArgumentException;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Defines values for the <code>properties</code> parameter of the {@link OpenTok#createSession(SessionProperties)} method.
 *
 * @see <a href="../OpenTokSDK.html#createSession(com.opentok.SessionProperties)">OpenTokSDK.createSession(SessionProperties)</a>
 */
public class SessionProperties {


    private String location = null;
    private boolean p2p = false;

    private SessionProperties(Builder builder) {
        this.location = builder.location;
        this.p2p = builder.p2p;
    }

    public static class Builder {
        private String location = null;
        private boolean p2p = false;
        

        public Builder location(String location) throws InvalidArgumentException {
            if (!InetAddressValidator.getInstance().isValidInet4Address(location)) {
                throw new InvalidArgumentException("Location must be a valid IPv4 address. location = " + location);
            }
            this.location = location;
            return this;
        }

        public Builder p2p(boolean p2p) {
            this.p2p = p2p;
            return this;
        }

        public SessionProperties build() {
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

    public Map<String, Collection<String>> toMap() {
        Map<String, Collection<String>> params = new HashMap<String, Collection<String>>();
        if (null != location) {
            ArrayList<String> valueList = new ArrayList<String>();
            valueList.add(location);
            params.put("location", valueList);
        }
        if (p2p) {
            ArrayList<String> valueList = new ArrayList<String>();
            valueList.add("enabled");
            params.put("p2p.preference", valueList);
        }
        return params;
    }

};
