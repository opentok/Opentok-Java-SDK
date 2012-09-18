/*
 * These unit tests require the opentok Java SDK.
 * https://github.com/opentok/Opentok-Java-SDK.git
 * 
 */

package com.opentok.test;

import com.opentok.api.API_Config;
import com.opentok.api.OpenTokSDK;
import com.opentok.api.OpenTokSession;
import com.opentok.api.constants.SessionProperties;
import com.opentok.api.constants.RoleConstants;
import com.opentok.exception.OpenTokException;
import com.opentok.util.TokBoxXML;
import java.util.*;
import java.net.*;
import java.io.*;

import junit.framework.Assert;
import org.junit.Test;

public class UnitTest {
	
    private OpenTokSDK sdk;

    private int apiKey;
    private String apiSecret;

    public UnitTest() {
        apiKey = Integer.valueOf(System.getProperty("apiKey"));
        apiSecret = System.getProperty("apiSecret");
		sdk = new OpenTokSDK(apiKey, apiSecret);
    }

    private TokBoxXML get_session_info(String session_id) throws OpenTokException {
    	String token = sdk.generate_token(session_id);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-TB-TOKEN-AUTH", token );
		TokBoxXML xml;
        xml = new TokBoxXML(request(API_Config.API_URL + "/session/" + session_id + "?extended=true", new HashMap<String, String>(), headers));
	    return xml;
    }

    private TokBoxXML get_token_info(String token) throws OpenTokException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-TB-TOKEN-AUTH",token);
		TokBoxXML xml;
	    xml = new TokBoxXML(request(API_Config.API_URL + "/token/validate", new HashMap<String, String>(), headers));
	    return xml;
    }
    
    @Test
	public void testCreateSesionNoParams() throws OpenTokException {
        OpenTokSession session = sdk.create_session();
        TokBoxXML xml = get_session_info(session.session_id);
        String expected = session.session_id;
        String actual = xml.getElementValue("session_id", "Session");
        Assert.assertEquals("Java SDK tests: Session create with no params failed", expected, actual);
    }
    
    @Test
	public void testCreateSesionWithLocation() throws OpenTokException {
		OpenTokSession session = sdk.create_session("216.38.134.114");
        TokBoxXML xml = get_session_info(session.session_id);
        String expected = session.session_id;
        String actual = xml.getElementValue("session_id", "Session");
        Assert.assertEquals("Java SDK tests: Session create with location failed", expected, actual);
    }

    @Test
    public void testP2PPreferenceEnable() throws OpenTokException {
		String expected = "enabled";
	    SessionProperties sp = new SessionProperties();
	    sp.p2p_preference = expected;
        OpenTokSession s = sdk.create_session("216.38.134.114", sp);
        TokBoxXML xml = get_session_info(s.session_id);
        String actual = xml.getElementValue("preference", "p2p");
        Assert.assertEquals("Java SDK tests: p2p not enabled", expected, actual);
    }

    @Test
    public void testP2PPreferenceDisable() throws OpenTokException {
		String expected = "disabled";
	    SessionProperties sp = new SessionProperties();
	    sp.p2p_preference = expected;
        OpenTokSession s = sdk.create_session("216.38.134.114", sp);
        TokBoxXML xml = get_session_info(s.session_id);
        String actual = xml.getElementValue("preference", "p2p");
        Assert.assertEquals("Java SDK tests: p2p not disabled", expected, actual);
	}

    @Test
    public void testRoleDefault() throws OpenTokException {
        String s= sdk.create_session().session_id;
        String t = sdk.generate_token(s);
        TokBoxXML xml = get_token_info(t);
        
        String expectedRole = "publisher";
        String actualRole = xml.getElementValue("role", "token").trim();
        Assert.assertEquals("Java SDK tests: role default not default (publisher)", expectedRole, actualRole);

        // Permissions are set as an empty node in the xml
        // Verify that the expected permission node is there
        // Verify nodes for permissions not granted to the role are not there
        Assert.assertTrue("Java SDK tests: default role does not have subscriber permissions", xml.hasElement("subscribe", "permissions"));
        Assert.assertTrue("Java SDK tests: default role does not have publisher permissions", xml.hasElement("publish", "permissions"));
        Assert.assertTrue("Java SDK tests: default role does not have signal permissions", xml.hasElement("signal", "permissions"));
        Assert.assertFalse("Java SDK tests: default role should not have forceunpublish permissions", xml.hasElement("forceunpublish", "permissions"));
        Assert.assertFalse("Java SDK tests: default role should not have forcedisconnect permissions", xml.hasElement("forcedisconnect", "permissions"));
        Assert.assertFalse("Java SDK tests: default role should not have record permissions", xml.hasElement("record", "permissions"));
        Assert.assertFalse("Java SDK tests: default role should not have playback permissions", xml.hasElement("playback", "permissions"));
    }

    @Test
    public void testRolePublisher() throws OpenTokException {
		String s= sdk.create_session().session_id;
        String t = sdk.generate_token(s, RoleConstants.PUBLISHER);
        TokBoxXML xml = get_token_info(t);
        
        String expectedRole = "publisher";
        String actualRole = xml.getElementValue("role", "token").trim();
        Assert.assertEquals("Java SDK tests: role not publisher", expectedRole, actualRole);
        
        // Permissions are set as an empty node in the xml
        // Verify that the expected permission node is there
        // Verify nodes for permissions not granted to the role are not there
        Assert.assertTrue("Java SDK tests: publisher role does not have subscriber permissions", xml.hasElement("subscribe", "permissions"));
        Assert.assertTrue("Java SDK tests: publisher role does not have publisher permissions", xml.hasElement("publish", "permissions"));
        Assert.assertTrue("Java SDK tests: publisher role does not have signal permissions", xml.hasElement("signal", "permissions"));
        Assert.assertFalse("Java SDK tests: publisher role should not have forceunpublish permissions", xml.hasElement("forceunpublish", "permissions"));
        Assert.assertFalse("Java SDK tests: publisher role should not have forcedisconnect permissions", xml.hasElement("forcedisconnect", "permissions"));
        Assert.assertFalse("Java SDK tests: publisher role should not have record permissions", xml.hasElement("record", "permissions"));
        Assert.assertFalse("Java SDK tests: publisher role should not have playback permissions", xml.hasElement("playback", "permissions"));
    }

    @Test
    public void testRoleSubscriber() throws OpenTokException {
		String s= sdk.create_session().session_id;
        String t = sdk.generate_token(s, RoleConstants.SUBSCRIBER);
        TokBoxXML xml = get_token_info(t);
        
        String expectedRole = "subscriber";
        String actualRole = xml.getElementValue("role", "token").trim();
        Assert.assertEquals("Java SDK tests: role not subscriber", expectedRole, actualRole);
        
        // Permissions are set as an empty node in the xml
        // Verify that the expected permission node is there
        // Verify nodes for permissions not granted to the role are not there
        Assert.assertTrue("Java SDK tests: subscriber role does not have subscriber permissions", xml.hasElement("subscribe", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have publisher permissions", xml.hasElement("publish", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have signal permissions", xml.hasElement("signal", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have forceunpublish permissions", xml.hasElement("forceunpublish", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have forcedisconnect permissions", xml.hasElement("forcedisconnect", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have record permissions", xml.hasElement("record", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have playback permissions", xml.hasElement("playback", "permissions"));
    }

    @Test
    public void testRoleModerator() throws OpenTokException {
		String s= sdk.create_session().session_id;
        String t = sdk.generate_token(s, RoleConstants.MODERATOR);
        TokBoxXML xml = get_token_info(t);
        
        String expectedRole = "moderator";
        String actualRole = xml.getElementValue("role", "token").trim();
        Assert.assertEquals("Java SDK tests: role not moderator", expectedRole, actualRole);
        
        // Permissions are set as an empty node in the xml
        // Verify that the expected permission node is there
        // Verify nodes for permissions not granted to the role are not there
        Assert.assertTrue("Java SDK tests: moderator role does not have subscriber permissions", xml.hasElement("subscribe", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have publisher permissions", xml.hasElement("publish", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have signal permissions", xml.hasElement("signal", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have forceunpublish permissions", xml.hasElement("forceunpublish", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have forcedisconnect permissions", xml.hasElement("forcedisconnect", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have record permissions", xml.hasElement("record", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have playback permissions", xml.hasElement("playback", "permissions"));
    }

    @Test
    public void testRoleGarbageInput() {
    	OpenTokException expected = null;
		try {
            String s= sdk.create_session().session_id;
            String t = sdk.generate_token(s, "asdfasdf");
		} catch (OpenTokException e) {
			expected = e;
        }
		Assert.assertNotNull("Java SDK tests: exception should be thrown for role asdfasdf", expected);
    }

    @Test
    public void testRoleNull() {
    	OpenTokException expected = null;
		try {
            String s= sdk.create_session().session_id;
            sdk.generate_token(s, null);
		} catch (OpenTokException e) {
			expected = e;
        }
		Assert.assertNotNull("Java SDK tests: exception should be thrown for role null", expected);
    }
    
    @Test
    public void testTokenExpireTimeDefault() throws OpenTokException {
        String s= sdk.create_session().session_id;
        String t = sdk.generate_token(s, RoleConstants.MODERATOR);
        TokBoxXML xml = get_token_info(t);
        Assert.assertFalse("Java SDK tests: expire_time should not exist for default", xml.hasElement("expire_time", "token"));
    }

    @Test
    public void testTokenExpireTimePast() {
    	OpenTokException expected = null;
		try {
            String s= sdk.create_session().session_id;
            sdk.generate_token(s, RoleConstants.MODERATOR, new Date().getTime() / 1000 - 100);
		} catch (OpenTokException e) {
			expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for expire time in past", expected);
    }
    
    @Test
    public void testTokenExpireTimeNow() throws OpenTokException {
    	long expireTime = new Date().getTime() / 1000;
    	String expected = "Token expired on " + expireTime;
        String s = sdk.create_session().session_id;
        String t = sdk.generate_token(s, RoleConstants.MODERATOR, expireTime);
        // Allow the token to expire.
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// do nothing
		}
        TokBoxXML xml = get_token_info(t);
        String actual = xml.getElementValue("invalid", "token");
        Assert.assertEquals("Java SDK tests: unexpected invalid token message", expected, actual);
    }
    
    @Test
    public void testTokenExpireTimeNearFuture() throws OpenTokException {
		long expected = new Date().getTime() / 1000 + 34200;
        String s= sdk.create_session().session_id;
        String t = sdk.generate_token(s, RoleConstants.MODERATOR, expected);
        TokBoxXML xml = get_token_info(t);
        long actual = new Long(xml.getElementValue("expire_time", "token").trim());
        Assert.assertEquals("Java SDK tests: expire time not set to expected time", expected, actual);
	}

    @Test
    public void testTokenExpireTimeFarFuture() {
    	OpenTokException expected = null;
		try {
            String s= sdk.create_session().session_id;
            sdk.generate_token(s, RoleConstants.MODERATOR, new Date().getTime() + 604800000);
		} catch (OpenTokException e) {
			expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for expire time more than 7 days in future", expected);
    }

    @Test
    public void testConnectionData() throws OpenTokException {
        String expected = "test string";
        String actual = null;
		String s= sdk.create_session().session_id;
        String t = sdk.generate_token(s, RoleConstants.PUBLISHER, null, expected);
        TokBoxXML xml = get_token_info(t);
        actual = xml.getElementValue("connection_data", "token").trim();
		Assert.assertEquals("Java SDK tests: connection data not set", expected, actual);
    }

    @Test
    public void testConnectionDataTooLarge() {
    	OpenTokException expected = null;
        String test_string = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc" +
                "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
                "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "gggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg" +
                "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh" +
                "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii" +
                "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" +
                "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk" +
                "llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll" +
                "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn" +
                "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo";
		try {
            String s= sdk.create_session().session_id;
            sdk.generate_token(s, RoleConstants.PUBLISHER, new Date().getTime(), test_string);
		} catch (OpenTokException e) {
		    expected = e;
        }
		Assert.assertNotNull("Java SDK tests: connection data over 1000 characters should not be accepted. Test String: " + test_string , expected);
    }
    
    @Test
    public void testCreateTokenWithOldSession() throws OpenTokException {
        String expected = "publisher";
        String actual = null;
        String s = "1abc70a34d069d2e6a1e565f3958b5250b435e32";
        String t = sdk.generate_token(s);
        TokBoxXML xml = get_token_info(t);
        actual = xml.getElementValue("role", "token").trim();
        Assert.assertEquals("Java SDK tests: token with old session not properly created. Session ID: " + s + ". Token: " + t, expected, actual);
    }
    
    
	public String request(String reqString, Map<String, String> paramList, Map<String, String> headers){

		StringBuilder returnString = new StringBuilder();

		URL url = null;
		HttpURLConnection conn = null;
		BufferedReader br = null;
		OutputStreamWriter wr = null;
		BufferedWriter bufWriter = null;

		try {

			StringBuilder dataString = new StringBuilder();

			for(Iterator<String> i = paramList.keySet().iterator(); i.hasNext(); ) {
				String key = i.next();
				String value = paramList.get(key);

				if(null != value) {
					value = URLEncoder.encode(paramList.get(key), "UTF-8").replaceAll("\\+", "%20");
					dataString.append(URLEncoder.encode(key, "UTF-8")).append("=").append(value).append("&");
				}
			}
			url = new URL(reqString);
			conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Content-Length", Integer.toString(dataString.toString().length()));
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestProperty("Accept", "text/html, application/xhtml+xml,application/xml");

			for(Iterator<String> i = headers.keySet().iterator(); i.hasNext(); ) {
				String key = i.next();
				String value = headers.get(key);
				conn.setRequestProperty(key, value);
			}

			wr = new OutputStreamWriter(conn.getOutputStream(), "UTF8");
			bufWriter = new BufferedWriter( wr );
			bufWriter.write(dataString.toString());
			bufWriter.flush();

			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF8"));

			String str;
			while(null != ((str = br.readLine())))
				{
					returnString.append(str);
					returnString.append("\n");
				}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(null != conn) {
					conn.disconnect();
				}

				if(null != wr) {
					wr.close();
				}

				if(null != bufWriter) {
					bufWriter.close();
				}

				if(null != br) {
					br.close();
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}

		return returnString.toString();
	}
}
