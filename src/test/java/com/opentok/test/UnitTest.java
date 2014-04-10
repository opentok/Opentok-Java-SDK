/*
 * These unit tests require the opentok Java SDK.
 * https://github.com/opentok/Opentok-Java-SDK.git
 * 
 */

package com.opentok.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;
import com.opentok.api.OpenTok;
import com.opentok.api.Session;
import com.opentok.api.constants.RoleConstants;
import com.opentok.api.constants.SessionProperties;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.OpenTokRequestException;
import com.opentok.util.TokBoxXML;

public class UnitTest {

    private OpenTok sdk;

    private int apiKey;
    private String apiSecret;
    private final String apiUrl = "https://api.tokbox.com";
    private AsyncHttpClient client;

    public UnitTest() {
        apiKey = Integer.valueOf(System.getProperty("apiKey"));
        apiSecret = System.getProperty("apiSecret");
        sdk = new OpenTok(apiKey, apiSecret);
        client = new AsyncHttpClient();
    }

    private TokBoxXML get_session_info(String session_id) throws OpenTokException {
        String token = sdk.generateToken(session_id);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-TB-TOKEN-AUTH", token );
        TokBoxXML xml;
        xml = new TokBoxXML(makePostRequest("/session/" + session_id + "?extended=true", headers, new HashMap<String, String>(), null));
        return xml;
    }

    private TokBoxXML get_token_info(String token) throws OpenTokException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-TB-TOKEN-AUTH",token);
        TokBoxXML xml;
        xml = new TokBoxXML(makePostRequest("/token/validate", headers, new HashMap<String, String>(), null));
        return xml;
    }

    @Test
    public void testCreateSesionNoParams() throws OpenTokException {
        Session session = sdk.createSession();
        TokBoxXML xml = get_session_info(session.getSessionId());
        String expected = session.getSessionId();
        String actual = xml.getElementValue("session_id", "Session");
        Assert.assertEquals("Java SDK tests: Session create with no params failed", expected, actual);
    }

    @Test
    public void testCreateSesionWithLocation() throws OpenTokException {
        SessionProperties properties = new SessionProperties.Builder()
                                            .location("216.38.134.114")
                                            .build();
        Session session = sdk.createSession(properties);
        TokBoxXML xml = get_session_info(session.getSessionId());
        String expected = session.getSessionId();
        String actual = xml.getElementValue("session_id", "Session");
        Assert.assertEquals("Java SDK tests: Session create with location failed", expected, actual);
    }

    @Test
    public void testP2PEnable() throws OpenTokException {
        String expected = "enabled";
        SessionProperties properties = new SessionProperties.Builder()
                                            .location("216.38.134.114")
                                            .p2p(true)
                                            .build();
        Session s = sdk.createSession(properties);
        TokBoxXML xml = get_session_info(s.getSessionId());
        String actual = xml.getElementValue("preference", "p2p");
        Assert.assertEquals("Java SDK tests: p2p not enabled", expected, actual);
    }

    @Test
    public void testRoleDefault() throws OpenTokException {
        Session s= sdk.createSession();
        String t = s.generateToken();
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
        Session s= sdk.createSession();
        String t = s.generateToken(RoleConstants.PUBLISHER);
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
        Session s= sdk.createSession();
        String t = s.generateToken(RoleConstants.SUBSCRIBER);
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
        Session s= sdk.createSession();
        String t = s.generateToken(RoleConstants.MODERATOR);
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
            Session s= sdk.createSession();
            s.generateToken("asdfasdf");
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for role asdfasdf", expected);
    }

    @Test
    public void testRoleNull() {
        OpenTokException expected = null;
        try {
            Session s= sdk.createSession();
            s.generateToken(null);
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for role null", expected);
    }

    @Test
    public void testTokenNullSessionId() throws OpenTokException {
        OpenTokException expected = null;
        try {
            sdk.generateToken(null);
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for null sessionId", expected);
    }


    public void testTokenEmptySessionId() throws OpenTokException {
        OpenTokException expected = null;
        try {
            sdk.generateToken("");
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for empty sessionId", expected);
    }

    @Test
    public void testTokenIncompleteSessionId() throws OpenTokException {
        OpenTokException expected = null;
        try {
            sdk.generateToken("jkasjda2ndasd");
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for invalid sessionId", expected);
    }

    @Test
    public void testTokenExpireTimeDefault() throws OpenTokException {
        Session s= sdk.createSession();
        String t = s.generateToken(RoleConstants.MODERATOR);
        TokBoxXML xml = get_token_info(t);
        Assert.assertFalse("Java SDK tests: expire_time should not exist for default", xml.hasElement("expire_time", "token"));
    }

    @Test
    public void testTokenExpireTimePast() {
        OpenTokException expected = null;
        try {
            Session s= sdk.createSession();
            s.generateToken(RoleConstants.MODERATOR, new Date().getTime() / 1000 - 100);
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for expire time in past", expected);
    }

    @Test
    public void testTokenExpireTimeNow() throws OpenTokException {
        long expireTime = new Date().getTime() / 1000;
        String expected = "Token expired on " + expireTime;
        Session s = sdk.createSession();
        String t = s.generateToken(RoleConstants.MODERATOR, expireTime);
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
        Session s= sdk.createSession();
        String t = s.generateToken(RoleConstants.MODERATOR, expected);
        TokBoxXML xml = get_token_info(t);
        long actual = new Long(xml.getElementValue("expire_time", "token").trim());
        Assert.assertEquals("Java SDK tests: expire time not set to expected time", expected, actual);
    }

    @Test
    public void testTokenExpireTimeFarFuture() {
        OpenTokException expected = null;
        try {
            Session s= sdk.createSession();
            s.generateToken(RoleConstants.MODERATOR, new Date().getTime() + 604800000);
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for expire time more than 7 days in future", expected);
    }

    @Test
    public void testConnectionData() throws OpenTokException {
        String expected = "test string";
        String actual = null;
        Session s= sdk.createSession();
        String t = s.generateToken(RoleConstants.PUBLISHER, 0, expected);
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
            Session s= sdk.createSession();
            s.generateToken(RoleConstants.PUBLISHER, new Date().getTime(), test_string);
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: connection data over 1000 characters should not be accepted. Test String: " + test_string , expected);
    }

    private String makePostRequest(String resource, Map<String, String> headers, Map<String, String> params,
            String postData) throws OpenTokException {
        BoundRequestBuilder post = this.client.preparePost(apiUrl + resource);
        if (params != null) {
            for (Entry<String, String> pair : params.entrySet()) {
                post.addParameter(pair.getKey(), pair.getValue());
            }
        }

        if (headers != null) {
            for (Entry<String, String> pair : headers.entrySet()) {
                post.addHeader(pair.getKey(), pair.getValue());
            }
        }

        post.addHeader("X-TB-PARTNER-AUTH", String.format("%s:%s", apiKey, apiSecret));
        post.addHeader("X-TB-VERSION", "1");

        if (postData != null) {
            post.setBody(postData);
        }

        try {
            Response result = post.execute().get();

            if (result.getStatusCode() < 200 || result.getStatusCode() > 299) {
                throw new OpenTokRequestException(result.getStatusCode(), result.getStatusText());
            }

            return result.getResponseBody();
        } catch (Exception e) {
            throw new OpenTokRequestException(500, "Error response: message: " + e.getMessage());
        }
    }
}
