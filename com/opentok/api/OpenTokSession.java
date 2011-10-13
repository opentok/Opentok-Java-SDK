
/*!
* OpenTok Java Library
* http://www.tokbox.com/
*
* Copyright 2010, TokBox, Inc.
*
*/
package com.opentok.api;

public class OpenTokSession {

	public String session_id;

	public OpenTokSession(String session_id) {
		this.session_id = session_id;
	}

	public String getSessionId() {
		return this.session_id;
	}
}