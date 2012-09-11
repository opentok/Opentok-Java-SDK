
/*!
* OpenTok Java Library
* http://www.tokbox.com/
*
* Copyright 2010, TokBox, Inc.
*
* Last modified: @opentok.sdk.java.mod_time@
*/

package com.opentok.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map;

import com.opentok.api.constants.SessionProperties;
import com.opentok.api.constants.RoleConstants;
import com.opentok.exception.OpenTokException;
import com.opentok.util.Base64;
import com.opentok.util.GenerateMac;
import com.opentok.util.TokBoxXML;
import com.opentok.api.OpenTokSession;

public class OpenTokSDK {

	protected int api_key;
	protected String api_secret;

	public OpenTokSDK(int api_key, String api_secret) {
		this.api_key = api_key;
		this.api_secret = api_secret.trim();
	}

	/**
	 *
     * Generate a token which is passed to the JS API to enable widgets to connect to the Opentok api.
	 *
	 * @session_id: Specify a session_id to make this token only valid for that session_id.
     * @role: One of the constants defined in RoleConstants. Default is publisher, look in the documentation to learn more about roles.
     * @expire_time: Integer timestamp. You can override the default token expire time of 24h by choosing an explicit expire time. Can be up to 7d after create_time.
	 */
    public String generate_token(String session_id, String role, Long expire_time, String connection_data) throws OpenTokException {
		Long create_time = new Long(System.currentTimeMillis() / 1000).longValue();
		StringBuilder data_string_builder = new StringBuilder();
		//Build the string
		Random random = new Random();
		int nonce = random.nextInt();
		data_string_builder.append("session_id=");
		data_string_builder.append(session_id);
		data_string_builder.append("&create_time=");
		data_string_builder.append(create_time);
		data_string_builder.append("&nonce=");
		data_string_builder.append(nonce);
		data_string_builder.append("&role=");
		data_string_builder.append(role);

		if(!RoleConstants.SUBSCRIBER.equals(role) &&
		    !RoleConstants.PUBLISHER.equals(role) &&
		    !RoleConstants.MODERATOR.equals(role) &&
		    !"".equals(role))
		    throw new OpenTokException(role + " is not a recognized role");

		if(expire_time != null) {
		    if(expire_time < (System.currentTimeMillis() / 1000)-1)
				throw new OpenTokException("Expire time must be in the future");
		    if(expire_time > (System.currentTimeMillis() / 1000 + 2592000))
				throw new OpenTokException("Expire time must be in the next 30 days");
			data_string_builder.append("&expire_time=");
			data_string_builder.append(expire_time);
		}
		if (connection_data != null) {
		    if(connection_data.length() > 1000)
		        throw new OpenTokException("Connection data must be less than 1000 characters");
			data_string_builder.append("&connection_data=");
			try {
				data_string_builder.append(URLEncoder.encode(connection_data, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Error during URL encode of your connection_data.", e);
			};
		}


		StringBuilder token_string_builder = new StringBuilder();
		try {
			token_string_builder.append("T1==");

			StringBuilder inner_builder = new StringBuilder();
			inner_builder.append("partner_id=");
			inner_builder.append(this.api_key);

			inner_builder.append("&sig=");

			inner_builder.append(GenerateMac.calculateRFC2104HMAC(data_string_builder.toString(),
																  this.api_secret));
			inner_builder.append(":");
			inner_builder.append(data_string_builder.toString());

			token_string_builder.append(Base64.encode(inner_builder.toString()));

		}catch (java.security.SignatureException e) {
				throw new OpenTokException(e.getMessage());
		}

		return token_string_builder.toString();
	}

	/**
	 * Creates a new session.
	 * @location: IP address to geolocate the call around.
	 * @session_properties: Optional array, keys are defined in SessionPropertyConstants
	 */

    public OpenTokSession create_session(String location, SessionProperties properties) throws OpenTokException {
		Map<String, String> params;
		if(properties != null)
			params = properties.to_map();
		else
			params = new HashMap<String, String>();

		return this.create_session(location, params);
	}

	/**
	 * Overloaded functions
	 * These work the same as those defined above, but with optional params filled in with defaults
	 */

	public String generate_token(String session_id) throws OpenTokException {
		return this.generate_token(session_id, RoleConstants.PUBLISHER, null, null);
	}


	public String generate_token(String session_id, String role) throws OpenTokException {
		return this.generate_token(session_id, role, null, null);
	}

	public String generate_token(String session_id, String role, Long expire_time) throws OpenTokException {
		return this.generate_token(session_id, role, expire_time, null);
	}

    public OpenTokSession create_session() throws OpenTokException {
		return create_session(null, new HashMap<String, String>());
	}


    public OpenTokSession create_session(String location) throws OpenTokException {
		return create_session(location, new HashMap<String, String>());
	}

    public OpenTokSession create_session(String location, Map<String, String> params) throws OpenTokException {
		params.put("location", location);
		TokBoxXML xmlResponse = this.do_request("/session/create", params);
		if(xmlResponse.hasElement("error", "Errors")) {
			throw new OpenTokException("Unable to create session");
		}
		String session_id = xmlResponse.getElementValue("session_id", "Session");
		return new OpenTokSession(session_id);
	}

	protected TokBoxXML do_request(String url, Map<String, String> params) throws OpenTokException {
		TokBoxNetConnection n = new TokBoxNetConnection();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-TB-PARTNER-AUTH", this.api_key + ":" + this.api_secret);

		return new TokBoxXML(n.request(API_Config.API_URL + url, params, headers));
	}

	protected static String join(List<String> s, String delimiter) throws java.io.UnsupportedEncodingException{
		if (s.isEmpty()) return "";
		Iterator<String> iter = s.iterator();
		StringBuffer buffer = new StringBuffer(URLEncoder.encode(iter.next(),"UTF-8"));
		while (iter.hasNext()) buffer.append(delimiter).append(URLEncoder.encode(iter.next(),"UTF-8"));
		return buffer.toString();
	}
}
