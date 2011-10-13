
/*!
* OpenTok Java Library
* http://www.tokbox.com/
*
* Copyright 2010, TokBox, Inc.
*/

package com.opentok.api.constants;
///List of valid roles for a token
public class RoleConstants {
    public static final String SUBSCRIBER = "subscriber"; //Can only subscribe
	public static final String PUBLISHER = "publisher";   //Can publish, subscribe, and signal
	public static final String MODERATOR = "moderator";   //Can do the above along with  forceDisconnect and forceUnpublish
}