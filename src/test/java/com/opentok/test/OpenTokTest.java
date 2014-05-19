package com.opentok.test;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
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
        assertEquals(MediaMode.ROUTED, session.getProperties().mediaMode());
        assertNull(session.getProperties().getLocation());

        verify(postRequestedFor(urlMatching("/session/create"))
                // TODO: add p2p.preference=disabled
                .withHeader("X-TB-PARTNER-AUTH", matching(this.apiKey+":"+this.apiSecret))
                .withHeader("User-Agent", matching(".*Opentok-Java-SDK/"+ Version.VERSION+".*")));
    }

    @Test
    public void testCreateRelayedSession() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo("/session/create"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><sessions><Session><" +
                                "session_id>" + sessionId + "</session_id><partner_id>123456</partner_id><create_dt>" +
                                "Mon Mar 17 00:41:31 PDT 2014</create_dt></Session></sessions>")));

        SessionProperties properties = new SessionProperties.Builder()
                .mediaMode(MediaMode.RELAYED)
                .build();
        Session session = sdk.createSession(properties);

        assertNotNull(session);
        assertEquals(this.apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());
        assertEquals(MediaMode.RELAYED, session.getProperties().mediaMode());
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
        assertEquals(MediaMode.ROUTED, session.getProperties().mediaMode());
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
        assertEquals(Long.toString(inOneDay), defaultTokenData.get("expire_time"));
        Map<String, String> oneHourTokenData = Helpers.decodeToken(oneHourToken);
        assertEquals(Long.toString(inOneHour), oneHourTokenData.get("expire_time"));
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

    @Test
    public void testListArchives() throws OpenTokException {

        stubFor(get(urlEqualTo("/v2/partner/"+this.apiKey+"/archive"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "          \"count\" : 6,\n" +
                                "          \"items\" : [ {\n" +
                                "            \"createdAt\" : 1395187930000,\n" +
                                "            \"duration\" : 22,\n" +
                                "            \"id\" : \"ef546c5a-4fd7-4e59-ab3d-f1cfb4148d1d\",\n" +
                                "            \"name\" : \"\",\n" +
                                "            \"partnerId\" : 123456,\n" +
                                "            \"reason\" : \"\",\n" +
                                "            \"sessionId\" : \"SESSIONID\",\n" +
                                "            \"size\" : 2909274,\n" +
                                "            \"status\" : \"available\",\n" +
                                "            \"url\" : \"http://tokbox.com.archive2.s3.amazonaws.com/123456%2Fef546c5" +
                                "a-4fd7-4e59-ab3d-f1cfb4148d1d%2Farchive.mp4?Expires=1395188695&AWSAccessKeyId=AKIAI6" +
                                "LQCPIXYVWCQV6Q&Signature=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\"\n" +
                                "          }, {\n" +
                                "            \"createdAt\" : 1395187910000,\n" +
                                "            \"duration\" : 14,\n" +
                                "            \"id\" : \"5350f06f-0166-402e-bc27-09ba54948512\",\n" +
                                "            \"name\" : \"\",\n" +
                                "            \"partnerId\" : 123456,\n" +
                                "            \"reason\" : \"\",\n" +
                                "            \"sessionId\" : \"SESSIONID\",\n" +
                                "            \"size\" : 1952651,\n" +
                                "            \"status\" : \"available\",\n" +
                                "            \"url\" : \"http://tokbox.com.archive2.s3.amazonaws.com/123456%2F5350f06" +
                                "f-0166-402e-bc27-09ba54948512%2Farchive.mp4?Expires=1395188695&AWSAccessKeyId=AKIAI6" +
                                "LQCPIXYVWCQV6Q&Signature=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\"\n" +
                                "          }, {\n" +
                                "            \"createdAt\" : 1395187836000,\n" +
                                "            \"duration\" : 62,\n" +
                                "            \"id\" : \"f6e7ee58-d6cf-4a59-896b-6d56b158ec71\",\n" +
                                "            \"name\" : \"\",\n" +
                                "            \"partnerId\" : 123456,\n" +
                                "            \"reason\" : \"\",\n" +
                                "            \"sessionId\" : \"SESSIONID\",\n" +
                                "            \"size\" : 8347554,\n" +
                                "            \"status\" : \"available\",\n" +
                                "            \"url\" : \"http://tokbox.com.archive2.s3.amazonaws.com/123456%2Ff6e7ee5" +
                                "8-d6cf-4a59-896b-6d56b158ec71%2Farchive.mp4?Expires=1395188695&AWSAccessKeyId=AKIAI6" +
                                "LQCPIXYVWCQV6Q&Signature=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\"\n" +
                                "          }, {\n" +
                                "            \"createdAt\" : 1395183243000,\n" +
                                "            \"duration\" : 544,\n" +
                                "            \"id\" : \"30b3ebf1-ba36-4f5b-8def-6f70d9986fe9\",\n" +
                                "            \"name\" : \"\",\n" +
                                "            \"partnerId\" : 123456,\n" +
                                "            \"reason\" : \"\",\n" +
                                "            \"sessionId\" : \"SESSIONID\",\n" +
                                "            \"size\" : 78499758,\n" +
                                "            \"status\" : \"available\",\n" +
                                "            \"url\" : \"http://tokbox.com.archive2.s3.amazonaws.com/123456%2F30b3ebf" +
                                "1-ba36-4f5b-8def-6f70d9986fe9%2Farchive.mp4?Expires=1395188695&AWSAccessKeyId=AKIAI6" +
                                "LQCPIXYVWCQV6Q&Signature=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\"\n" +
                                "          }, {\n" +
                                "            \"createdAt\" : 1394396753000,\n" +
                                "            \"duration\" : 24,\n" +
                                "            \"id\" : \"b8f64de1-e218-4091-9544-4cbf369fc238\",\n" +
                                "            \"name\" : \"showtime again\",\n" +
                                "            \"partnerId\" : 123456,\n" +
                                "            \"reason\" : \"\",\n" +
                                "            \"sessionId\" : \"SESSIONID\",\n" +
                                "            \"size\" : 2227849,\n" +
                                "            \"status\" : \"available\",\n" +
                                "            \"url\" : \"http://tokbox.com.archive2.s3.amazonaws.com/123456%2Fb8f64de" +
                                "1-e218-4091-9544-4cbf369fc238%2Farchive.mp4?Expires=1395188695&AWSAccessKeyId=AKIAI6" +
                                "LQCPIXYVWCQV6Q&Signature=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\"\n" +
                                "          }, {\n" +
                                "            \"createdAt\" : 1394321113000,\n" +
                                "            \"duration\" : 1294,\n" +
                                "            \"id\" : \"832641bf-5dbf-41a1-ad94-fea213e59a92\",\n" +
                                "            \"name\" : \"showtime\",\n" +
                                "            \"partnerId\" : 123456,\n" +
                                "            \"reason\" : \"\",\n" +
                                "            \"sessionId\" : \"SESSIONID\",\n" +
                                "            \"size\" : 42165242,\n" +
                                "            \"status\" : \"available\",\n" +
                                "            \"url\" : \"http://tokbox.com.archive2.s3.amazonaws.com/123456%2F832641b" +
                                "f-5dbf-41a1-ad94-fea213e59a92%2Farchive.mp4?Expires=1395188695&AWSAccessKeyId=AKIAI6" +
                                "LQCPIXYVWCQV6Q&Signature=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\"\n" +
                                "          } ]\n" +
                                "        }")));

        List<Archive> archives = sdk.listArchives();

        // NOTE: what about archive totalCount (total number of archives for API Key)?
        assertNotNull(archives);
        assertEquals(6, archives.size());

        verify(getRequestedFor(urlMatching("/v2/partner/"+this.apiKey+"/archive"))
                .withHeader("X-TB-PARTNER-AUTH", matching(this.apiKey+":"+this.apiSecret))
                .withHeader("User-Agent", matching(".*Opentok-Java-SDK/"+ Version.VERSION+".*")));
    }

    // TODO: test list archives with count and offset

    // TODO: test list archives failure scenarios

    @Test
    public void testStartArchive() throws OpenTokException {
        String sessionId = "SESSIONID";

        stubFor(post(urlEqualTo("/v2/partner/"+this.apiKey+"/archive"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "          \"createdAt\" : 1395183243556,\n" +
                                "          \"duration\" : 0,\n" +
                                "          \"id\" : \"30b3ebf1-ba36-4f5b-8def-6f70d9986fe9\",\n" +
                                "          \"name\" : \"\",\n" +
                                "          \"partnerId\" : 123456,\n" +
                                "          \"reason\" : \"\",\n" +
                                "          \"sessionId\" : \"SESSIONID\",\n" +
                                "          \"size\" : 0,\n" +
                                "          \"status\" : \"started\",\n" +
                                "          \"url\" : null\n" +
                                "        }")));

        Archive archive = sdk.startArchive(sessionId, null);
        assertNotNull(archive);
        assertEquals(sessionId, archive.getSessionId());
        assertNotNull(archive.getId());

        verify(postRequestedFor(urlMatching("/v2/partner/"+this.apiKey+"/archive"))
                // TODO: find a way to match JSON without caring about spacing
                //.withRequestBody(matching(".*"+".*"))
                .withHeader("X-TB-PARTNER-AUTH", matching(this.apiKey+":"+this.apiSecret))
                .withHeader("User-Agent", matching(".*Opentok-Java-SDK/"+ Version.VERSION+".*")));
    }

    // TODO: test start archive with name

    // TODO: test start archive failure scenarios

    @Test
    public void testStopArchive() throws OpenTokException {
        String archiveId = "ARCHIVEID";

        stubFor(post(urlEqualTo("/v2/partner/"+this.apiKey+"/archive/"+archiveId+"/stop"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "          \"createdAt\" : 1395183243000,\n" +
                                "          \"duration\" : 0,\n" +
                                "          \"id\" : \"ARCHIVEID\",\n" +
                                "          \"name\" : \"\",\n" +
                                "          \"partnerId\" : 123456,\n" +
                                "          \"reason\" : \"\",\n" +
                                "          \"sessionId\" : \"SESSIONID\",\n" +
                                "          \"size\" : 0,\n" +
                                "          \"status\" : \"stopped\",\n" +
                                "          \"url\" : null\n" +
                                "        }")));

        Archive archive = sdk.stopArchive(archiveId);
        assertNotNull(archive);
        assertEquals("SESSIONID", archive.getSessionId());
        assertEquals(archiveId, archive.getId());

        verify(postRequestedFor(urlMatching("/v2/partner/"+this.apiKey+"/archive/"+archiveId+"/stop"))
                .withHeader("X-TB-PARTNER-AUTH", matching(this.apiKey+":"+this.apiSecret))
                .withHeader("User-Agent", matching(".*Opentok-Java-SDK/"+ Version.VERSION+".*")));
    }

    // TODO: test stop archive failure scenarios

    @Test
    public void testDeleteArchive() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        stubFor(delete(urlEqualTo("/v2/partner/"+this.apiKey+"/archive/"+archiveId))
                .willReturn(aResponse()
                        .withStatus(204)
                        .withHeader("Content-Type", "application/json")));

        sdk.deleteArchive(archiveId);

        verify(deleteRequestedFor(urlMatching("/v2/partner/"+this.apiKey+"/archive/"+archiveId))
                .withHeader("X-TB-PARTNER-AUTH", matching(this.apiKey+":"+this.apiSecret))
                .withHeader("User-Agent", matching(".*Opentok-Java-SDK/"+ Version.VERSION+".*")));
    }

    // TODO: test delete archive failure scenarios

}
