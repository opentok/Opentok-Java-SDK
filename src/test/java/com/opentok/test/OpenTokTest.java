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
import java.util.ArrayList;
import java.util.Map;

import com.opentok.*;
import org.apache.commons.lang.StringUtils;

import com.opentok.constants.Version;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.InvalidArgumentException;

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

    @Test(expected = InvalidArgumentException.class)
    public void testCreateBadSession() throws OpenTokException {
            SessionProperties properties = new SessionProperties.Builder()
                    .location("NOT A VALID IP")
                    .build();
    }

    // TODO: test session creation conditions that result in errors

    @Test
    public void testTokenDefault() throws
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

    @Test
    public void testTokenRoles() throws
            OpenTokException, UnsupportedEncodingException, NoSuchAlgorithmException, SignatureException,
            InvalidKeyException {

        int apiKey = 123456;
        String apiSecret = "1234567890abcdef1234567890abcdef1234567890";
        OpenTok opentok = new OpenTok(apiKey, apiSecret);
        String sessionId = "1_MX4xMjM0NTZ-flNhdCBNYXIgMTUgMTQ6NDI6MjMgUERUIDIwMTR-MC40OTAxMzAyNX4";
        Role role = Role.SUBSCRIBER;

        String defaultToken = opentok.generateToken(sessionId);
        String roleToken = opentok.generateToken(sessionId, new TokenOptions.Builder()
                .role(role)
                .build());

        assertNotNull(defaultToken);
        assertNotNull(roleToken);
        assertTrue(Helpers.verifyTokenSignature(defaultToken, apiSecret));
        assertTrue(Helpers.verifyTokenSignature(roleToken, apiSecret));

        Map<String, String> defaultTokenData = Helpers.decodeToken(defaultToken);
        assertEquals("publisher", defaultTokenData.get("role"));
        Map<String, String> roleTokenData = Helpers.decodeToken(roleToken);
        assertEquals(role.toString(), roleTokenData.get("role"));
    }

    @Test
    public void testTokenExpireTime() throws
            OpenTokException, SignatureException, NoSuchAlgorithmException, InvalidKeyException,
            UnsupportedEncodingException {

        int apiKey = 123456;
        String apiSecret = "1234567890abcdef1234567890abcdef1234567890";
        String sessionId = "1_MX4xMjM0NTZ-flNhdCBNYXIgMTUgMTQ6NDI6MjMgUERUIDIwMTR-MC40OTAxMzAyNX4";
        OpenTok opentok = new OpenTok(apiKey, apiSecret);
        long now = System.currentTimeMillis() / 1000L;
        long inOneHour = now + (60*60);
        long inOneDay = now + (60*60*24);
        long inThirtyDays = now + (60*60*24*30);
        ArrayList<Exception> exceptions = new ArrayList<Exception>();


        String defaultToken = opentok.generateToken(sessionId);
        String oneHourToken = opentok.generateToken(sessionId, new TokenOptions.Builder()
            .expireTime(inOneHour)
            .build());
        try {
            String earlyExpireTimeToken = opentok.generateToken(sessionId, new TokenOptions.Builder()
            .expireTime(now - 10)
            .build());
        } catch (Exception exception) {
            exceptions.add(exception);
        }
        try {
            String lateExpireTimeToken = opentok.generateToken(sessionId, new TokenOptions.Builder()
                .expireTime(inThirtyDays+(60*60*24) /* 31 days */)
                .build());
        } catch (Exception exception) {
            exceptions.add(exception);
        }

        assertNotNull(defaultToken);
        assertNotNull(oneHourToken);
        assertTrue(Helpers.verifyTokenSignature(defaultToken, apiSecret));
        assertTrue(Helpers.verifyTokenSignature(oneHourToken, apiSecret));

        Map<String, String> defaultTokenData = Helpers.decodeToken(defaultToken);
        assertEquals(Double.toString(inOneDay), defaultTokenData.get("expire_time"));
        Map<String, String> oneHourTokenData = Helpers.decodeToken(oneHourToken);
        assertEquals(Double.toString(inOneHour), oneHourTokenData.get("expire_time"));
        assertEquals(2, exceptions.size());
        for (Exception e : exceptions) {
            assertEquals(InvalidArgumentException.class, e.getClass());
        }

    }

    @Test
    public void testTokenConnectionData() throws
            OpenTokException, SignatureException, NoSuchAlgorithmException, InvalidKeyException,
            UnsupportedEncodingException {

        int apiKey = 123456;
        String apiSecret = "1234567890abcdef1234567890abcdef1234567890";
        String sessionId = "1_MX4xMjM0NTZ-flNhdCBNYXIgMTUgMTQ6NDI6MjMgUERUIDIwMTR-MC40OTAxMzAyNX4";
        OpenTok opentok = new OpenTok(apiKey, apiSecret);
        // purposely contains some exotic characters
        String actualData = "{\"name\":\"%foo ç &\"}";
        Exception tooLongException = null;

        String defaultToken = opentok.generateToken(sessionId);
        String dataBearingToken = opentok.generateToken(sessionId, new TokenOptions.Builder()
            .data(actualData)
            .build());
        try {
            String dataTooLongToken = opentok.generateToken(sessionId, new TokenOptions.Builder()
                    .data(StringUtils.repeat("x", 1001))
                    .build());
        } catch (InvalidArgumentException e) {
            tooLongException = e;
        }

        assertNotNull(defaultToken);
        assertNotNull(dataBearingToken);
        assertTrue(Helpers.verifyTokenSignature(defaultToken, apiSecret));
        assertTrue(Helpers.verifyTokenSignature(dataBearingToken, apiSecret));

        Map<String, String> defaultTokenData = Helpers.decodeToken(defaultToken);
        assertNull(defaultTokenData.get("connection_data"));
        Map<String, String> dataBearingTokenData = Helpers.decodeToken(dataBearingToken);
        assertEquals(actualData, dataBearingTokenData.get("connection_data"));
        assertEquals(InvalidArgumentException.class, tooLongException.getClass());
    }

    @Test
    public void testTokenBadSessionId() throws OpenTokException {
        int apiKey = 123456;
        String apiSecret = "1234567890abcdef1234567890abcdef1234567890";
        OpenTok opentok = new OpenTok(apiKey, apiSecret);
        ArrayList<Exception> exceptions = new ArrayList<Exception>();

        try {
            String nullSessionToken = opentok.generateToken(null);
        } catch (Exception e) {
            exceptions.add(e);
        }
        try {
            String emptySessionToken = opentok.generateToken("");
        } catch (Exception e) {
            exceptions.add(e);
        }
        try {
            String invalidSessionToken = opentok.generateToken("NOT A VALID SESSION ID");
        } catch (Exception e) {
            exceptions.add(e);
        }

        assertEquals(3, exceptions.size());
        for (Exception e : exceptions) {
            assertEquals(InvalidArgumentException.class, e.getClass());
        }
    }

    @Test
    public void testGetArchive() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        stubFor(get(urlEqualTo("/v2/partner/"+this.apiKey+"/archive/"+archiveId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "          \"createdAt\" : 1395187836000,\n" +
                                "          \"duration\" : 62,\n" +
                                "          \"id\" : \"" + archiveId + "\",\n" +
                                "          \"name\" : \"\",\n" +
                                "          \"partnerId\" : 123456,\n" +
                                "          \"reason\" : \"\",\n" +
                                "          \"sessionId\" : \"SESSIONID\",\n" +
                                "          \"size\" : 8347554,\n" +
                                "          \"status\" : \"available\",\n" +
                                "          \"url\" : \"http://tokbox.com.archive2.s3.amazonaws.com/123456%2F" +
                                archiveId + "%2Farchive.mp4?Expires=1395194362&AWSAccessKeyId=AKIAI6LQCPIXYVWCQV6Q&Si" +
                                "gnature=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\"\n" +
                                "        }")));

        Archive archive = sdk.getArchive(archiveId);

        assertNotNull(archive);
        assertEquals(this.apiKey, archive.getPartnerId());
        assertEquals(archiveId, archive.getId());
        assertEquals(1395187836000L, archive.getCreatedAt());
        assertEquals(62, archive.getDuration());
        assertEquals("", archive.getName());
        assertEquals("SESSIONID", archive.getSessionId());
        assertEquals(8347554, archive.getSize());
        assertEquals(Archive.Status.AVAILABLE, archive.getStatus());
        assertEquals("http://tokbox.com.archive2.s3.amazonaws.com/123456%2F"+archiveId +"%2Farchive.mp4?Expires=13951" +
                "94362&AWSAccessKeyId=AKIAI6LQCPIXYVWCQV6Q&Signature=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", archive.getUrl());

        verify(getRequestedFor(urlMatching("/v2/partner/"+this.apiKey+"/archive/"+archiveId))
                .withHeader("X-TB-PARTNER-AUTH", matching(this.apiKey+":"+this.apiSecret))
                .withHeader("User-Agent", matching(".*Opentok-Java-SDK/"+ Version.VERSION+".*")));
    }

    // TODO: test get archive failure scenarios

}
