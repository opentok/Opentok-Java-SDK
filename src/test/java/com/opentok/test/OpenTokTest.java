/*
 * These unit tests require the opentok Java SDK.
 * https://github.com/opentok/Opentok-Java-SDK.git
 * 
 */

package com.opentok.test;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.opentok.test.Helpers;

import com.opentok.api.OpenTok;
import com.opentok.api.Session;
import com.opentok.api.constants.Version;
import com.opentok.api.constants.RoleConstants;
import com.opentok.api.constants.SessionProperties;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.OpenTokInvalidArgumentException;
import com.opentok.exception.OpenTokRequestException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class OpenTokTest {

    private int apiKey = 123456;
    private String apiSecret = "1234567890abcdef1234567890abcdef1234567890";
    private String apiUrl = "http://localhost:8080";
    private OpenTok sdk;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    @Before
    public void setUp() {

        // read system properties for integration testing
        int anApiKey = 0;
        boolean useMockKey = false;
        String anApiKeyProp = System.getProperty("apiKey");
        String anApiSecret = System.getProperty("apiSecret");
        try {
            anApiKey = Integer.parseInt(anApiKeyProp);
        } catch (NumberFormatException e) {
            useMockKey = true;
        }

        if (!useMockKey && anApiSecret != null && !anApiSecret.isEmpty()) {
            // TODO: figure out when to turn mocking off based on this
            apiKey = anApiKey;
            apiSecret = anApiSecret;
        }
        sdk = new OpenTok(apiKey, apiSecret, apiUrl);
    }

    @Test
    public void testCreateDefaultSession() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo("/session/create"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><sessions><Session><" +
                                "session_id>" + sessionId + "</session_id><partner_id>123456</partner_id><create_dt>" +
                                "Mon Mar 17 00:41:31 PDT 2014</create_dt></Session></sessions>")));

        Session session = sdk.createSession();

        assertNotNull(session);
        assertEquals(this.apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());
        assertFalse(session.getProperties().isP2p());
        assertNull(session.getProperties().getLocation());

        verify(postRequestedFor(urlMatching("/session/create"))
                // TODO: add p2p.preference=disabled
                .withHeader("X-TB-PARTNER-AUTH", matching(this.apiKey+":"+this.apiSecret))
                .withHeader("User-Agent", matching(".*Opentok-Java-SDK/"+ Version.VERSION+".*")));
    }

    @Test
    public void testCreateP2pSession() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo("/session/create"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><sessions><Session><" +
                                "session_id>" + sessionId + "</session_id><partner_id>123456</partner_id><create_dt>" +
                                "Mon Mar 17 00:41:31 PDT 2014</create_dt></Session></sessions>")));

        SessionProperties properties = new SessionProperties.Builder()
                .p2p(true)
                .build();
        Session session = sdk.createSession(properties);

        assertNotNull(session);
        assertEquals(this.apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());
        assertTrue(session.getProperties().isP2p());
        assertNull(session.getProperties().getLocation());

        verify(postRequestedFor(urlMatching("/session/create"))
                // TODO: this is a pretty bad way to verify, ideally we can decode the body and then query the object
                .withRequestBody(matching(".*p2p.preference=enabled.*"))
                .withHeader("X-TB-PARTNER-AUTH", matching(this.apiKey+":"+this.apiSecret))
                .withHeader("User-Agent", matching(".*Opentok-Java-SDK/"+ Version.VERSION+".*")));
    }

    @Test
    public void testCreateLocationHintSession() throws OpenTokException {
        String sessionId = "SESSIONID";
        String locationHint = "12.34.56.78";
        stubFor(post(urlEqualTo("/session/create"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><sessions><Session><" +
                                "session_id>" + sessionId + "</session_id><partner_id>123456</partner_id><create_dt>" +
                                "Mon Mar 17 00:41:31 PDT 2014</create_dt></Session></sessions>")));

        SessionProperties properties = new SessionProperties.Builder()
                .location(locationHint)
                .build();
        Session session = sdk.createSession(properties);

        assertNotNull(session);
        assertEquals(this.apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());
        assertFalse(session.getProperties().isP2p());
        assertEquals(locationHint, session.getProperties().getLocation());

        verify(postRequestedFor(urlMatching("/session/create"))
                // TODO: this is a pretty bad way to verify, ideally we can decode the body and then query the object
                .withRequestBody(matching(".*location="+locationHint+".*"))
                .withHeader("X-TB-PARTNER-AUTH", matching(this.apiKey+":"+this.apiSecret))
                .withHeader("User-Agent", matching(".*Opentok-Java-SDK/"+ Version.VERSION+".*")));
    }

    @Test(expected = OpenTokInvalidArgumentException.class)
    public void testCreateBadSession() throws OpenTokException {
            SessionProperties properties = new SessionProperties.Builder()
                    .location("NOT A VALID IP")
                    .build();
    }

    // TODO: test session creation conditions that result in errors

    @Test
    public void testRoleDefault() throws
            OpenTokException, UnsupportedEncodingException, NoSuchAlgorithmException, SignatureException,
            InvalidKeyException {

        int apiKey = 123456;
        String apiSecret = "1234567890abcdef1234567890abcdef1234567890";
        OpenTok opentok = new OpenTok(apiKey, apiSecret);
        String sessionId = "1_MX4xMjM0NTZ-flNhdCBNYXIgMTUgMTQ6NDI6MjMgUERUIDIwMTR-MC40OTAxMzAyNX4";

        String token = opentok.generateToken(sessionId);

        assertNotNull(token);
        assertTrue(Helpers.verifyTokenSignature(token, apiSecret));

        Map<String, String> tokenData = Helpers.decodeToken(token);
        assertEquals(Integer.toString(apiKey), tokenData.get("partner_id"));
        assertNotNull(tokenData.get("create_time"));
        assertNotNull(tokenData.get("nonce"));
    }

//    @Test
//    public void testRolePublisher() throws OpenTokException {
//        Session s= sdk.createSession();
//        String t = s.generateToken(RoleConstants.PUBLISHER);
//        TokBoxXML xml = get_token_info(t);
//
//        String expectedRole = "publisher";
//        String actualRole = xml.getElementValue("role", "token").trim();
//        Assert.assertEquals("Java SDK tests: role not publisher", expectedRole, actualRole);
//
//        // Permissions are set as an empty node in the xml
//        // Verify that the expected permission node is there
//        // Verify nodes for permissions not granted to the role are not there
//        Assert.assertTrue("Java SDK tests: publisher role does not have subscriber permissions", xml.hasElement("subscribe", "permissions"));
//        Assert.assertTrue("Java SDK tests: publisher role does not have publisher permissions", xml.hasElement("publish", "permissions"));
//        Assert.assertTrue("Java SDK tests: publisher role does not have signal permissions", xml.hasElement("signal", "permissions"));
//        Assert.assertFalse("Java SDK tests: publisher role should not have forceunpublish permissions", xml.hasElement("forceunpublish", "permissions"));
//        Assert.assertFalse("Java SDK tests: publisher role should not have forcedisconnect permissions", xml.hasElement("forcedisconnect", "permissions"));
//        Assert.assertFalse("Java SDK tests: publisher role should not have record permissions", xml.hasElement("record", "permissions"));
//        Assert.assertFalse("Java SDK tests: publisher role should not have playback permissions", xml.hasElement("playback", "permissions"));
//    }
//
//    @Test
//    public void testRoleSubscriber() throws OpenTokException {
//        Session s= sdk.createSession();
//        String t = s.generateToken(RoleConstants.SUBSCRIBER);
//        TokBoxXML xml = get_token_info(t);
//
//        String expectedRole = "subscriber";
//        String actualRole = xml.getElementValue("role", "token").trim();
//        Assert.assertEquals("Java SDK tests: role not subscriber", expectedRole, actualRole);
//
//        // Permissions are set as an empty node in the xml
//        // Verify that the expected permission node is there
//        // Verify nodes for permissions not granted to the role are not there
//        Assert.assertTrue("Java SDK tests: subscriber role does not have subscriber permissions", xml.hasElement("subscribe", "permissions"));
//        Assert.assertFalse("Java SDK tests: subscriber role should not have publisher permissions", xml.hasElement("publish", "permissions"));
//        Assert.assertFalse("Java SDK tests: subscriber role should not have signal permissions", xml.hasElement("signal", "permissions"));
//        Assert.assertFalse("Java SDK tests: subscriber role should not have forceunpublish permissions", xml.hasElement("forceunpublish", "permissions"));
//        Assert.assertFalse("Java SDK tests: subscriber role should not have forcedisconnect permissions", xml.hasElement("forcedisconnect", "permissions"));
//        Assert.assertFalse("Java SDK tests: subscriber role should not have record permissions", xml.hasElement("record", "permissions"));
//        Assert.assertFalse("Java SDK tests: subscriber role should not have playback permissions", xml.hasElement("playback", "permissions"));
//    }
//
//    @Test
//    public void testRoleModerator() throws OpenTokException {
//        Session s= sdk.createSession();
//        String t = s.generateToken(RoleConstants.MODERATOR);
//        TokBoxXML xml = get_token_info(t);
//
//        String expectedRole = "moderator";
//        String actualRole = xml.getElementValue("role", "token").trim();
//        Assert.assertEquals("Java SDK tests: role not moderator", expectedRole, actualRole);
//
//        // Permissions are set as an empty node in the xml
//        // Verify that the expected permission node is there
//        // Verify nodes for permissions not granted to the role are not there
//        Assert.assertTrue("Java SDK tests: moderator role does not have subscriber permissions", xml.hasElement("subscribe", "permissions"));
//        Assert.assertTrue("Java SDK tests: moderator role does not have publisher permissions", xml.hasElement("publish", "permissions"));
//        Assert.assertTrue("Java SDK tests: moderator role does not have signal permissions", xml.hasElement("signal", "permissions"));
//        Assert.assertTrue("Java SDK tests: moderator role does not have forceunpublish permissions", xml.hasElement("forceunpublish", "permissions"));
//        Assert.assertTrue("Java SDK tests: moderator role does not have forcedisconnect permissions", xml.hasElement("forcedisconnect", "permissions"));
//        Assert.assertTrue("Java SDK tests: moderator role does not have record permissions", xml.hasElement("record", "permissions"));
//        Assert.assertTrue("Java SDK tests: moderator role does not have playback permissions", xml.hasElement("playback", "permissions"));
//    }
//
//    @Test
//    public void testRoleGarbageInput() {
//        OpenTokException expected = null;
//        try {
//            Session s= sdk.createSession();
//            s.generateToken("asdfasdf");
//        } catch (OpenTokException e) {
//            expected = e;
//        }
//        Assert.assertNotNull("Java SDK tests: exception should be thrown for role asdfasdf", expected);
//    }
//
//    @Test
//    public void testRoleNull() {
//        OpenTokException expected = null;
//        try {
//            Session s= sdk.createSession();
//            s.generateToken(null);
//        } catch (OpenTokException e) {
//            expected = e;
//        }
//        Assert.assertNotNull("Java SDK tests: exception should be thrown for role null", expected);
//    }
//
//    @Test
//    public void testTokenNullSessionId() throws OpenTokException {
//        OpenTokException expected = null;
//        try {
//            sdk.generateToken(null);
//        } catch (OpenTokException e) {
//            expected = e;
//        }
//        Assert.assertNotNull("Java SDK tests: exception should be thrown for null sessionId", expected);
//    }
//
//
//    public void testTokenEmptySessionId() throws OpenTokException {
//        OpenTokException expected = null;
//        try {
//            sdk.generateToken("");
//        } catch (OpenTokException e) {
//            expected = e;
//        }
//        Assert.assertNotNull("Java SDK tests: exception should be thrown for empty sessionId", expected);
//    }
//
//    @Test
//    public void testTokenIncompleteSessionId() throws OpenTokException {
//        OpenTokException expected = null;
//        try {
//            sdk.generateToken("jkasjda2ndasd");
//        } catch (OpenTokException e) {
//            expected = e;
//        }
//        Assert.assertNotNull("Java SDK tests: exception should be thrown for invalid sessionId", expected);
//    }
//
//    @Test
//    public void testTokenExpireTimeDefault() throws OpenTokException {
//        Session s= sdk.createSession();
//        String t = s.generateToken(RoleConstants.MODERATOR);
//        TokBoxXML xml = get_token_info(t);
//        Assert.assertFalse("Java SDK tests: expire_time should not exist for default", xml.hasElement("expire_time", "token"));
//    }
//
//    @Test
//    public void testTokenExpireTimePast() {
//        OpenTokException expected = null;
//        try {
//            Session s= sdk.createSession();
//            s.generateToken(RoleConstants.MODERATOR, new Date().getTime() / 1000 - 100);
//        } catch (OpenTokException e) {
//            expected = e;
//        }
//        Assert.assertNotNull("Java SDK tests: exception should be thrown for expire time in past", expected);
//    }
//
//    @Test
//    public void testTokenExpireTimeNow() throws OpenTokException {
//        long expireTime = new Date().getTime() / 1000;
//        String expected = "Token expired on " + expireTime;
//        Session s = sdk.createSession();
//        String t = s.generateToken(RoleConstants.MODERATOR, expireTime);
//        // Allow the token to expire.
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            // do nothing
//        }
//        TokBoxXML xml = get_token_info(t);
//        String actual = xml.getElementValue("invalid", "token");
//        Assert.assertEquals("Java SDK tests: unexpected invalid token message", expected, actual);
//    }
//
//    @Test
//    public void testTokenExpireTimeNearFuture() throws OpenTokException {
//        long expected = new Date().getTime() / 1000 + 34200;
//        Session s= sdk.createSession();
//        String t = s.generateToken(RoleConstants.MODERATOR, expected);
//        TokBoxXML xml = get_token_info(t);
//        long actual = new Long(xml.getElementValue("expire_time", "token").trim());
//        Assert.assertEquals("Java SDK tests: expire time not set to expected time", expected, actual);
//    }
//
//    @Test
//    public void testTokenExpireTimeFarFuture() {
//        OpenTokException expected = null;
//        try {
//            Session s= sdk.createSession();
//            s.generateToken(RoleConstants.MODERATOR, new Date().getTime() + 604800000);
//        } catch (OpenTokException e) {
//            expected = e;
//        }
//        Assert.assertNotNull("Java SDK tests: exception should be thrown for expire time more than 7 days in future", expected);
//    }
//
//    @Test
//    public void testConnectionData() throws OpenTokException {
//        String expected = "test string";
//        String actual = null;
//        Session s= sdk.createSession();
//        String t = s.generateToken(RoleConstants.PUBLISHER, 0, expected);
//        TokBoxXML xml = get_token_info(t);
//        actual = xml.getElementValue("connection_data", "token").trim();
//        Assert.assertEquals("Java SDK tests: connection data not set", expected, actual);
//    }
//
//    @Test
//    public void testConnectionDataTooLarge() {
//        OpenTokException expected = null;
//        String test_string = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
//                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
//                "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc" +
//                "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
//                "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
//                "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
//                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
//                "gggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg" +
//                "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh" +
//                "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii" +
//                "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" +
//                "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk" +
//                "llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll" +
//                "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
//                "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn" +
//                "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo";
//        try {
//            Session s= sdk.createSession();
//            s.generateToken(RoleConstants.PUBLISHER, new Date().getTime(), test_string);
//        } catch (OpenTokException e) {
//            expected = e;
//        }
//        Assert.assertNotNull("Java SDK tests: connection data over 1000 characters should not be accepted. Test String: " + test_string , expected);
//    }

}
