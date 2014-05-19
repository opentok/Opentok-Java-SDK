package com.opentok;

import com.opentok.exception.InvalidArgumentException;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#createSession(SessionProperties)} method.
 *
 * @see OpenTok#createSession(com.opentok.SessionProperties properties)
 */
public class SessionProperties {


    private String location = null;
    private MediaMode mediaMode;

    private SessionProperties(Builder builder) {
        this.location = builder.location;
        this.mediaMode = builder.mediaMode;
    }

    /**
     * Use this class to create a SessionProperties object.
     *
     * @see SessionProperties
     */
    public static class Builder {
        private String location = null;
        private MediaMode mediaMode = MediaMode.ROUTED;
        

        /**
         * Call this method to set an IP address that the OpenTok servers will use to
         * situate the session in its global network. If you do not set a location hint,
         * the OpenTok servers will be based on the first client connecting to the session.
         *
         * @param location The IP address to serve as the locaion hint.
         *
         * @return The SessionProperties.Builder object with the location hint setting.
         */
        public Builder location(String location) throws InvalidArgumentException {
            if (!InetAddressValidator.getInstance().isValidInet4Address(location)) {
                throw new InvalidArgumentException("Location must be a valid IPv4 address. location = " + location);
            }
            this.location = location;
            return this;
        }

       /**
       * Call this method to determine whether the session will transmit streams between using the
       * OpenTok Media Router (MediaMode.ROUTED) or not (MediaMode.RELAYED). By default, sessions
       * use the OpenTok Media Router (false). If a session does not use the OpenTok Media Router,
       * clients will attempt to transmit streams directly to each other. If two clients cannot send
       * and receive each others' streams, due to firewalls on the clients' networks, their
       * streams will be relayed using the OpenTok TURN Server.
       * <p>
       * The <a href="http://www.tokbox.com/blog/mantis-next-generation-cloud-technology-for-webrtc/">
       * OpenTok Media Router</a> provides a number of benefits. For example, the OpenTok Media
       * Router can decrease bandwidth usage in multiparty sessions. Also, the OpenTok Media Router
       * can improve the quality of the user experience through
       * <a href="http://www.tokbox.com/blog/quality-of-experience-and-traffic-shaping-the-next-step-with-mantis/">dynamic
       * traffic shaping</a>.
       * <p>
       * With the mediaMode set to MediaMode.RELAYED, the session will attempt to transmit streams
       * directly between clients. If clients cannot connect due to firewall restrictions, the
       * session uses the OpenTok TURN server to relay audio-video streams.
       * <p>
       * You will be billed for streamed minutes if you use the OpenTok Media Router or if the
       * session uses the OpenTok TURN server to relay streams. For information on pricing, see the
       * <a href="http://www.tokbox.com/pricing">OpenTok pricing page</a>.
       *
       * @param mediaMode Set to a value defined in the MediaMode enum.
       *
       * @return The SessionProperties.Builder object with the peer-to-peer setting.
       */
        public Builder mediaMode(MediaMode mediaMode) {
            this.mediaMode = mediaMode;
            return this;
        }

        /**
         * Builds the SessionProperties object.
         *
         * @return The SessionProperties object.
         */
        public SessionProperties build() {
            return new SessionProperties(this);
        }
    }
    /**
    * The location hint IP address. See {@link SessionProperties.Builder#location(String location)}.
    */
    public String getLocation() {
        return location;
    }
    
    /**
     * Defines whether the session will transmit streams using the OpenTok Media Server or attempt
     * to transmit streams directly between clients. See
     * {@link SessionProperties.Builder#mediaMode(MediaMode mediaMode)}.
     */
    public MediaMode mediaMode() {
        return mediaMode;
    }

    /**
     * Returns the session properties as a Map.
     */
    public Map<String, Collection<String>> toMap() {
        Map<String, Collection<String>> params = new HashMap<String, Collection<String>>();
        if (null != location) {
            ArrayList<String> valueList = new ArrayList<String>();
            valueList.add(location);
            params.put("location", valueList);
        }
        ArrayList<String> valueList = new ArrayList<String>();
        valueList.add(mediaMode.toString());
        params.put("p2p.preference", valueList);

        return params;
    }

};
