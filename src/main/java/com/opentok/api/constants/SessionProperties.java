
/*!
* OpenTok Java Library
* http://www.tokbox.com/
*
* Copyright 2010, TokBox, Inc.
*/
package com.opentok.api.constants;

import java.util.*;

public class SessionProperties {

	public Boolean echoSuppression_enabled = null;
	public Integer multiplexer_numOutputStreams = null;
	public Integer multiplexer_switchType = null;
	public Integer multiplexer_switchTimeout = null;
	public Integer multiplexer_transitionDuration = null;
	public String p2p_preference = null;

	public Map<String, String> to_map() {
		Map<String, String> m = new HashMap<String, String>();
		if(this.echoSuppression_enabled != null)
			m.put("echoSuppression.enabled", this.echoSuppression_enabled.toString());
		if(this.multiplexer_numOutputStreams != null)
			m.put("multiplexer.numOutputStreams", this.multiplexer_numOutputStreams.toString());
		if(this.multiplexer_switchType != null)
			m.put("multiplexer.switchType", this.multiplexer_switchType.toString());
		if(this.multiplexer_switchTimeout != null)
			m.put("multiplexer.switchTimeout", this.multiplexer_switchTimeout.toString());
		if(this.multiplexer_transitionDuration != null)
			m.put("multiplexer.transitionDuration", this.multiplexer_transitionDuration.toString());
		if(this.p2p_preference != null)
			m.put("p2p.preference", this.p2p_preference);
		return m;
	}
};
