/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.opentok.*;
import com.opentok.Archive.OutputMode;
import com.opentok.constants.DefaultUserAgent;
import com.opentok.exception.InvalidArgumentException;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.RequestException;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

public class OpenTokTest {
    private final String SESSION_CREATE = "/session/create";
    private int apiKey = 123456;
    private String archivePath = "/v2/project/" + apiKey + "/archive";
    private String broadcastPath = "/v2/project/" + apiKey + "/broadcast";
    private String apiSecret = "1234567890abcdef1234567890abcdef1234567890";
    private String apiUrl = "http://localhost:8080";
    private OpenTok sdk;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    @Before
    public void setUp() throws OpenTokException {

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
            archivePath = "/v2/project/" + apiKey + "/archive";
        }
        sdk = new OpenTok.Builder(apiKey, apiSecret).apiUrl(apiUrl).build();
    }

    @Test
    public void testUserAgent() throws Exception {
        stubFor(post(anyUrl())
            .withHeader("User-Agent", equalTo(DefaultUserAgent.DEFAULT_USER_AGENT))
            .willReturn(aResponse().withStatus(200))
        );
        sdk.disableForceMute("SESSION_ID");
        verify(postRequestedFor(anyUrl()));

        sdk = new OpenTok.Builder(apiKey, apiSecret).apiUrl(apiUrl).appendToUserAgent("Test_UA").build();
        stubFor(post(anyUrl())
            .withHeader("User-Agent", equalTo(DefaultUserAgent.DEFAULT_USER_AGENT+" Test_UA"))
            .willReturn(aResponse().withStatus(200))
        );
        sdk.disableForceMute("SESSION_ID");
        verify(postRequestedFor(anyUrl()));
        WireMock.reset();
    }

    /**
     * Test that a request throws exception if request exceeds configured timeout
     */
    @Test
    public void testConfigureRequestTimeout() {
        assertThrows(RequestException.class, () -> {
            sdk.close();
            sdk = new OpenTok.Builder(apiKey, apiSecret).apiUrl(apiUrl).requestTimeout(6).build();

            String sessionId = "SESSIONID";
            stubFor(post(urlEqualTo(SESSION_CREATE))
                  .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(7000)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"session_id\":\"" + sessionId + "\",\"project_id\":\"00000000\"," +
                              "\"partner_id\":\"123456\"," +
                              "\"create_dt\":\"Mon Mar 17 00:41:31 PDT 2014\"," +
                              "\"media_server_url\":\"\"}]")));

            sdk.createSession();
        });
        CreatedSession createdSession = CreatedSession.makeSession();
        assertNull(createdSession.getCreateDt());
        assertNull(createdSession.getMediaServerURL());
        assertNull(createdSession.getProjectId());
        assertNull(createdSession.getPartnerId());
    }

    @Test
    public void testSignalAllConnections() throws OpenTokException {
        String sessionId = "SESSIONID";
        String path = "/v2/project/" + apiKey + "/session/" + sessionId + "/signal";
        stubFor(post(urlEqualTo(path))
              .willReturn(aResponse()
                    .withStatus(204)));
        SignalProperties properties = new SignalProperties.Builder()
              .type("test")
              .data("Signal test string")
              .build();

        properties.toMap();

        sdk.signal(sessionId, properties);
        verify(postRequestedFor(urlMatching(path)));
        verify(postRequestedFor(urlMatching(path))
              .withHeader("Content-Type", equalTo("application/json")));

        verify(postRequestedFor(urlMatching(path))
              .withRequestBody(equalToJson("{ \"type\":\"test\",\"data\":\"Signal test string\" }")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(path)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testSignalWithEmptySessionID() throws OpenTokException {
        String sessionId = "";

        SignalProperties properties = new SignalProperties.Builder().type("test").data("Signal test string").build();
        try {
            sdk.signal(sessionId, properties);
        } catch (InvalidArgumentException e) {
            assertEquals(e.getMessage(), "Session string null or empty");
        }
    }

    @Test
    public void testSignalWithEmoji() throws OpenTokException {
        String sessionId = "SESSIONID";
        String path = "/v2/project/" + apiKey + "/session/" + sessionId + "/signal";
        SignalProperties properties = new SignalProperties.Builder().type("test").data("\uD83D\uDE01").build();
        stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(413)));
        assertThrows(RequestException.class, () -> sdk.signal(sessionId, properties));
    }

    @Test
    public void testSignalSingleConnection() throws OpenTokException {
        String sessionId = "SESSIONID";
        String connectionId = "CONNECTIONID";
        String path = "/v2/project/" + apiKey + "/session/" + sessionId + "/connection/" + connectionId + "/signal";
        stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(204)));
        SignalProperties properties = new SignalProperties.Builder().type("test").data("Signal test string").build();
        sdk.signal(sessionId, connectionId, properties);

        verify(postRequestedFor(urlMatching(path)));
        verify(postRequestedFor(urlMatching(path))
              .withHeader("Content-Type", equalTo("application/json")));

        verify(postRequestedFor(urlMatching(path))
              .withRequestBody(equalToJson("{ \"type\":\"test\",\"data\":\"Signal test string\" }")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(path)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testSignalWithEmptyConnectionID() throws OpenTokException {
        String sessionId = "SESSIONID";
        String connectionId = "";

        SignalProperties properties = new SignalProperties.Builder().type("test").data("Signal test string").build();
        try {
            sdk.signal(sessionId, connectionId, properties);
        } catch (InvalidArgumentException e) {

            assertEquals(e.getMessage(), "Session or Connection string null or empty");
        }
    }

    @Test
    public void testSignalWithConnectionIDAndEmptySessionID() throws OpenTokException {
        String sessionId = "";
        String connectionId = "CONNECTIONID";

        SignalProperties properties = new SignalProperties.Builder().type("test").data("Signal test string").build();
        try {
            sdk.signal(sessionId, connectionId, properties);
        } catch (InvalidArgumentException e) {

            assertEquals(e.getMessage(), "Session or Connection string null or empty");
        }
    }

    @Test
    public void testSignalWithEmptySessionAndConnectionID() throws OpenTokException {
        String sessionId = "";
        String connectionId = "";

        SignalProperties properties = new SignalProperties.Builder().type("test").data("Signal test string").build();
        try {
            sdk.signal(sessionId, connectionId, properties);
        } catch (InvalidArgumentException e) {
            assertEquals(e.getMessage(), "Session or Connection string null or empty");
        }
    }

    @Test
    public void testCreateDefaultSession() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(SESSION_CREATE))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[{\"session_id\":\"" + sessionId + "\",\"project_id\":\"00000000\"," +
                          "\"partner_id\":\"123456\"," +
                          "\"create_dt\":\"Mon Mar 17 00:41:31 PDT 2014\"," +
                          "\"media_server_url\":\"\"}]")));

        Session session = sdk.createSession();

        assertNotNull(session);
        assertFalse(session.getProperties().isEndToEndEncrypted());
        assertEquals(apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());
        assertEquals(MediaMode.RELAYED, session.getProperties().mediaMode());
        assertEquals(ArchiveMode.MANUAL, session.getProperties().archiveMode());
        assertNull(session.getProperties().getLocation());

        verify(postRequestedFor(urlMatching(SESSION_CREATE))
              .withRequestBody(matching(".*p2p.preference=enabled.*"))
              .withRequestBody(matching(".*archiveMode=manual.*")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(SESSION_CREATE)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testCreateRoutedSession() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(SESSION_CREATE))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[{\"session_id\":\"" + sessionId + "\",\"project_id\":\"00000000\"," +
                          "\"partner_id\":\"123456\"," +
                          "\"create_dt\":\"Mon Mar 17 00:41:31 PDT 2014\"," +
                          "\"media_server_url\":\"\"}]")));

        SessionProperties properties = new SessionProperties.Builder()
              .mediaMode(MediaMode.ROUTED)
              .build();
        Session session = sdk.createSession(properties);

        assertNotNull(session);
        assertEquals(properties, session.getProperties());
        assertFalse(session.getProperties().isEndToEndEncrypted());
        assertEquals(apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());
        assertEquals(MediaMode.ROUTED, session.getProperties().mediaMode());
        assertNull(session.getProperties().getLocation());

        verify(postRequestedFor(urlMatching(SESSION_CREATE))
              // NOTE: this is a pretty bad way to verify, ideally we can decode the body and then query the object
              .withRequestBody(matching(".*p2p.preference=disabled.*")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(SESSION_CREATE)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testCreateLocationHintSession() throws OpenTokException {
        String sessionId = "SESSIONID";
        String locationHint = "12.34.56.78";
        stubFor(post(urlEqualTo(SESSION_CREATE))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[{\"session_id\":\"" + sessionId + "\",\"project_id\":\"00000000\"," +
                          "\"partner_id\":\"123456\"," +
                          "\"create_dt\":\"Mon Mar 17 00:41:31 PDT 2014\"," +
                          "\"media_server_url\":\"\"}]")));

        SessionProperties properties = new SessionProperties.Builder()
              .location(locationHint)
              .build();
        Session session = sdk.createSession(properties);

        assertNotNull(session);
        assertEquals(properties, session.getProperties());
        assertFalse(session.getProperties().isEndToEndEncrypted());
        assertEquals(apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());
        assertEquals(MediaMode.RELAYED, session.getProperties().mediaMode());
        assertEquals(locationHint, session.getProperties().getLocation());

        verify(postRequestedFor(urlMatching(SESSION_CREATE))
              // TODO: this is a pretty bad way to verify, ideally we can decode the body and then query the object
              .withRequestBody(matching(".*location=" + locationHint + ".*")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(SESSION_CREATE)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testCreateEncryptedSession() throws OpenTokException {
        String sessionId = "SESSION1D";
        stubFor(post(urlEqualTo(SESSION_CREATE))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[{\"session_id\":\"" + sessionId + "\",\"project_id\":\"00000000\"," +
                    "\"partner_id\":\"123456\"," +
                    "\"create_dt\":\"Mon Mar 17 00:41:31 PDT 2014\"," +
                    "\"media_server_url\":\"\"}]")));

        SessionProperties properties = new SessionProperties.Builder()
            .endToEndEncryption()
            .mediaMode(MediaMode.ROUTED)
            .build();
        Session session = sdk.createSession(properties);

        assertNotNull(session);
        assertEquals(properties, session.getProperties());
        assertTrue(session.getProperties().isEndToEndEncrypted());
        assertEquals(apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());
        assertEquals(MediaMode.ROUTED, session.getProperties().mediaMode());
        assertEquals(ArchiveMode.MANUAL, session.getProperties().archiveMode());
        assertNull(session.getProperties().getLocation());

        verify(postRequestedFor(urlMatching(SESSION_CREATE))
            // NOTE: this is a pretty bad way to verify, ideally we can decode the body and then query the object
            .withRequestBody(matching(".*e2ee=true.*")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
            findAll(postRequestedFor(urlMatching(SESSION_CREATE)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testCreateAlwaysArchivedSession() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(SESSION_CREATE))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[{\"session_id\":\"" + sessionId + "\",\"project_id\":\"00000000\"," +
                          "\"partner_id\":\"123456\"," +
                          "\"create_dt\":\"Mon Mar 17 00:41:31 PDT 2014\"," +
                          "\"media_server_url\":\"\"}]")));

        SessionProperties properties = new SessionProperties.Builder()
              .archiveMode(ArchiveMode.ALWAYS)
              .mediaMode(MediaMode.ROUTED)
              .archiveResolution(Resolution.HD_VERTICAL)
              .archiveName("720pTest")
              .build();
        Session session = sdk.createSession(properties);

        assertNotNull(session);
        assertEquals(apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());
        assertEquals(ArchiveMode.ALWAYS, session.getProperties().archiveMode());
        assertEquals(Resolution.HD_VERTICAL, session.getProperties().archiveResolution());

        verify(postRequestedFor(urlMatching(SESSION_CREATE))
              // TODO: this is a pretty bad way to verify, ideally we can decode the body and then query the object
              .withRequestBody(matching(".*archiveMode=always.*"))
              .withRequestBody(matching(".*archiveResolution=720x1280.*"))
              .withRequestBody(matching(".*archiveName=720pTest.*")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(SESSION_CREATE)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testAutoArchiveSessionValidation() {
        SessionProperties.Builder builder = new SessionProperties.Builder()
                .archiveMode(ArchiveMode.ALWAYS)
                .mediaMode(MediaMode.ROUTED);

        SessionProperties plain = builder.build();
        assertNull(plain.archiveName());
        assertNull(plain.archiveResolution());

        assertEquals(1, builder.archiveName("A").build().archiveName().length());
        assertThrows(IllegalArgumentException.class, () -> builder.archiveName("").build());
        StringBuilder sb = new StringBuilder(80);
        for (int i = 0; i < 10; sb.append("Archive").append(i++));
        assertEquals(80, builder.archiveName(sb.toString()).build().archiveName().length());
        assertThrows(IllegalArgumentException.class, () -> builder.archiveName(sb.append("N").toString()).build());

        builder.archiveName("Test").archiveMode(ArchiveMode.MANUAL);
        assertThrows(IllegalStateException.class, builder::build);

        SessionProperties fhd = builder
                .archiveMode(ArchiveMode.ALWAYS)
                .archiveResolution(Resolution.FHD_HORIZONTAL)
                .archiveName(null).build();
        assertEquals("1920x1080", fhd.archiveResolution().toString());
        assertNull(fhd.archiveName());

        assertThrows(IllegalStateException.class, () -> builder.archiveMode(ArchiveMode.MANUAL).build());
    }

    @Test(expected = InvalidArgumentException.class)
    public void testCreateBadSession() throws OpenTokException {
        SessionProperties properties = new SessionProperties.Builder()
              .location("NOT A VALID IP")
              .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateInvalidAlwaysArchivedAndRelayedSession() {
        new SessionProperties.Builder()
                .mediaMode(MediaMode.RELAYED)
                .archiveMode(ArchiveMode.ALWAYS)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateInvalidAlwaysArchivedAndE2eeSession() {
        new SessionProperties.Builder()
            .mediaMode(MediaMode.ROUTED)
            .endToEndEncryption()
            .archiveMode(ArchiveMode.ALWAYS)
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateInvalidRelayedMediaAndE2eeSession() {
        new SessionProperties.Builder()
            .archiveMode(ArchiveMode.MANUAL)
            .endToEndEncryption()
            .mediaMode(MediaMode.RELAYED)
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateInvalidE2eeSessionDefault() {
        new SessionProperties.Builder().endToEndEncryption().build();
    }

    @Test
    public void testTokenDefault() throws
          OpenTokException, UnsupportedEncodingException, NoSuchAlgorithmException,
          SignatureException, InvalidKeyException {

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
    public void testTokenLayoutClass() throws
          OpenTokException, UnsupportedEncodingException, NoSuchAlgorithmException,
          SignatureException, InvalidKeyException {

        String apiSecret = "1234567890abcdef1234567890abcdef1234567890";
        new OpenTok(123456, apiSecret);
        String sessionId = "1_MX4xMjM0NTZ-flNhdCBNYXIgMTUgMTQ6NDI6MjMgUERUIDIwMTR-MC40OTAxMzAyNX4";

        String token = sdk.generateToken(sessionId, new TokenOptions.Builder()
              .initialLayoutClassList(Arrays.asList("full", "focus"))
              .build());

        assertNotNull(token);
        assertTrue(Helpers.verifyTokenSignature(token, apiSecret));

        Map<String, String> tokenData = Helpers.decodeToken(token);
        assertEquals("full focus", tokenData.get("initial_layout_class_list"));
    }

    @Test
    public void testRoleStringValues() {
        for (Role role : Role.values()) {
            String roleStr = null;
            switch (role) {
                case MODERATOR: roleStr = "moderator"; break;
                case PUBLISHER: roleStr = "publisher"; break;
                case SUBSCRIBER: roleStr = "subscriber"; break;
                case PUBLISHER_ONLY: roleStr = "publisheronly"; break;
            }
            assertEquals(roleStr, role.toString());
        }
    }

    @Test
    public void testTokenRoles() throws Exception {

        int apiKey = 123456;
        String apiSecret = "1234567890abcdef1234567890abcdef1234567890";
        OpenTok opentok = new OpenTok(apiKey, apiSecret);
        String sessionId = "1_MX4xMjM0NTZ-flNhdCBNYXIgMTUgMTQ6NDI6MjMgUERUIDIwMTR-MC40OTAxMzAyNX4";

        for (Role role : Role.values()) {
            String roleToken = sdk.generateToken(sessionId,
                    new TokenOptions.Builder().role(role).build()
            );

            assertNotNull(roleToken);
            assertTrue(Helpers.verifyTokenSignature(roleToken, apiSecret));
            Map<String, String> roleTokenData = Helpers.decodeToken(roleToken);
            assertEquals(role.toString(), roleTokenData.get("role"));
        }

        String defaultToken = opentok.generateToken(sessionId);
        assertNotNull(defaultToken);
        assertTrue(Helpers.verifyTokenSignature(defaultToken, apiSecret));
        Map<String, String> defaultTokenData = Helpers.decodeToken(defaultToken);
        assertEquals("publisher", defaultTokenData.get("role"));
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
        long inOneHour = now + (60 * 60);
        long inOneDay = now + (60 * 60 * 24);
        long inThirtyDays = now + (60 * 60 * 24 * 30);
        ArrayList<Exception> exceptions = new ArrayList<>();

        String defaultToken = opentok.generateToken(sessionId);
        String oneHourToken = opentok.generateToken(sessionId, new TokenOptions.Builder()
              .expireTime(inOneHour)
              .build());
        try {
            opentok.generateToken(sessionId, new TokenOptions.Builder()
                  .expireTime(now - 10)
                  .build());
        } catch (Exception exception) {
            exceptions.add(exception);
        }
        try {
            opentok.generateToken(sessionId, new TokenOptions.Builder()
                  .expireTime(inThirtyDays + (60 * 60 * 24) /* 31 days */)
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
        ArrayList<Exception> exceptions = new ArrayList<>();

        try {
            opentok.generateToken(null);
        } catch (Exception e) {
            exceptions.add(e);
        }
        try {
            opentok.generateToken("");
        } catch (Exception e) {
            exceptions.add(e);
        }
        try {
            opentok.generateToken("NOT A VALID SESSION ID");
        } catch (Exception e) {
            exceptions.add(e);
        }

        assertEquals(3, exceptions.size());
        for (Exception e : exceptions) {
            assertEquals(InvalidArgumentException.class, e.getClass());
        }
    }

    /* TODO: find a way to match JSON without caring about spacing
    .withRequestBody(matching("."+".")) in the following archive tests   */

    @Test
    public void testGetArchive() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        stubFor(get(urlEqualTo(archivePath + "/" + archiveId))
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
        assertNotNull(archive.toString());
        assertNull(archive.getPassword());
        assertNotNull(archive.getStreamMode());
        assertNotNull(archive.getReason());
        assertTrue(archive.hasAudio());
        assertTrue(archive.hasVideo());
        assertEquals(apiKey, archive.getPartnerId());
        assertEquals(archiveId, archive.getId());
        assertEquals(1395187836000L, archive.getCreatedAt());
        assertEquals(62, archive.getDuration());
        assertEquals("", archive.getName());
        assertEquals("SESSIONID", archive.getSessionId());
        assertEquals(8347554, archive.getSize());
        assertEquals(Archive.Status.AVAILABLE, archive.getStatus());
        assertEquals("http://tokbox.com.archive2.s3.amazonaws.com/123456%2F" + archiveId + "%2Farchive.mp4?Expires=13951" +
              "94362&AWSAccessKeyId=AKIAI6LQCPIXYVWCQV6Q&Signature=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", archive.getUrl());

        verify(getRequestedFor(urlMatching(archivePath + "/" + archiveId)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(getRequestedFor(urlMatching(archivePath + "/" + archiveId)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testPatchArchive() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        String streamId = "abc123efg456";
        stubFor(patch(urlEqualTo(archivePath + "/" + archiveId + "/streams"))
              .willReturn(aResponse().withStatus(204)));
        sdk.addArchiveStream(archiveId, streamId, true, true);
        verify(patchRequestedFor(urlMatching(archivePath + "/" + archiveId + "/streams")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(deleteRequestedFor(urlMatching(archivePath + "/" + archiveId)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testPatchArchiveExpectException() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        Exception exception = assertThrows(OpenTokException.class, () -> sdk.removeArchiveStream(archiveId, ""));
        String got = exception.getMessage();
        String expected = "Could not patch archive, needs one of: addStream or removeStream";
        assertEquals(expected, got);
    }

    // TODO: test get archive failure scenarios
    @Test
    public void testListArchives() throws OpenTokException {
        stubFor(get(urlEqualTo(archivePath))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"count\" : 60,\n" +
                          "          \"items\" : [ {\n" +
                          "            \"createdAt\" : 1395187930000,\n" +
                          "            \"duration\" : 22,\n" +
                          "            \"id\" : \"ef546c5a-4fd7-4e59-ab3d-f1cfb4148d1d\",\n" +
                          "            \"name\" : \"\",\n" +
                          "            \"partnerId\" : 123456,\n" +
                          "            \"reason\" : \"\",\n" +
                          "            \"sessionId\" : \"SESSIONID\",\n" +
                          "            \"size\" : 247145329511,\n" +
                          "            \"status\" : \"available\",\n" +
                          "            \"url\" : \"http://tokbox.com.archive2.s3.amazonaws.com/123456%2Fef546c5" +
                          "a-4fd7-4e59-ab3d-f1cfb4148d1d%2Farchive.mp4?Expires=1395188695&AWSAccessKeyId=AKIAI6" +
                          "LQCPIXYVWCQV6Q&Signature=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\"\n" +
                          "          }, {\n" +
                          "            \"createdAt\" : 1395187910000,\n" +
                          "            \"duration\" : 14,\n" +
                          "            \"id\" : \"5350f06f-0166-402e-bc27-09ba54948512\",\n" +
                          "            \"name\" : \"\",\n" +
                          "            \"multiArchiveTag\" : \"MyVideoArchiveTag\",\n" +
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
        ArchiveList archives = sdk.listArchives();
        assertNotNull(archives);
        assertEquals(6, archives.size());
        assertEquals(60, archives.getTotalCount());
        assertNotNull(archives.get(0));
        assertEquals("ef546c5a-4fd7-4e59-ab3d-f1cfb4148d1d", archives.get(0).getId());
        assertEquals("MyVideoArchiveTag", archives.get(1).getMultiArchiveTag());
        verify(getRequestedFor(urlMatching(archivePath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(getRequestedFor(urlMatching(archivePath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testListArchivesWithOffSetCount() throws OpenTokException {
        String sessionId = "SESSIONID";
        String url = archivePath + "?offset=1&count=1";
        stubFor(get(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"count\" : 60,\n" +
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
                          "          }]\n" +
                          "        }")));

        ArchiveList archives = sdk.listArchives(1, 1);
        assertNotNull(archives);
        assertEquals(1, archives.size());
        assertEquals(60, archives.getTotalCount());
        assertNotNull(archives.get(0));
        assertEquals("ef546c5a-4fd7-4e59-ab3d-f1cfb4148d1d", archives.get(0).getId());

        verify(getRequestedFor(urlEqualTo(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(getRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testListArchivesWithSessionIdOffSetCount() throws OpenTokException {
        String sessionId = "SESSIONID";
        String url = archivePath + "?offset=1&count=1&sessionId=" + sessionId;
        stubFor(get(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"count\" : 60,\n" +
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
                          "          }]\n" +
                          "        }")));
        ArchiveList archives = sdk.listArchives(sessionId, 1, 1);
        assertNotNull(archives);
        assertEquals(1, archives.size());
        assertEquals(60, archives.getTotalCount());
        assertNotNull(archives.get(0));
        assertEquals("ef546c5a-4fd7-4e59-ab3d-f1cfb4148d1d", archives.get(0).getId());
        verify(getRequestedFor(urlEqualTo(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(getRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testListArchivesWithSessionId() throws OpenTokException {
        String sessionId = "SESSIONID";
        String url = archivePath + "?sessionId=" + sessionId;
        stubFor(get(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"count\" : 60,\n" +
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
        ArchiveList archives = sdk.listArchives(sessionId);
        assertNotNull(archives);
        assertEquals(6, archives.size());
        assertEquals(60, archives.getTotalCount());
        assertNotNull(archives.get(0));
        assertEquals("ef546c5a-4fd7-4e59-ab3d-f1cfb4148d1d", archives.get(0).getId());
        verify(getRequestedFor(urlEqualTo(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(getRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testListArchivesWithEmptySessionID() throws OpenTokException {
        int exceptionCount = 0;
        int testCount = 2;
        try {
            sdk.listArchives("");
        } catch (InvalidArgumentException e) {
            assertEquals(e.getMessage(), "Session Id cannot be null or empty");
            exceptionCount++;
        }
        try {
            sdk.listArchives(null);
        } catch (InvalidArgumentException e) {
            assertEquals(e.getMessage(), "Session Id cannot be null or empty");
            exceptionCount++;
        }
        assertEquals(exceptionCount, testCount);
    }

    @Test
    public void testListArchivesWithWrongOffsetCountValues() throws OpenTokException {
        int exceptionCount = 0;
        int testCount = 4;
        try {
            ArchiveList archives = sdk.listArchives(-2, 0);
        } catch (InvalidArgumentException e) {
            assertEquals(e.getMessage(), "Make sure count parameter value is >= 0 and/or offset parameter value is <=1000");
            exceptionCount++;
        }
        try {
            sdk.listArchives(0, 1200);
        } catch (InvalidArgumentException e) {
            assertEquals(e.getMessage(), "Make sure count parameter value is >= 0 and/or offset parameter value is <=1000");
            exceptionCount++;
        }
        try {
            sdk.listArchives(-10, 12);
        } catch (InvalidArgumentException e) {
            assertEquals(e.getMessage(), "Make sure count parameter value is >= 0 and/or offset parameter value is <=1000");
            exceptionCount++;
        }
        try {
            ArchiveList archives = sdk.listArchives(-10, 1200);
        } catch (InvalidArgumentException e) {
            assertEquals(e.getMessage(), "Make sure count parameter value is >= 0 and/or offset parameter value is <=1000");
            exceptionCount++;
        }
        assertEquals(exceptionCount, testCount);
    }

    @Test
    public void testStartArchive() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(archivePath))
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
        ArchiveProperties properties = new ArchiveProperties.Builder()
              .name(null)
              .hasAudio(true)
              .hasVideo(false)
              .outputMode(OutputMode.COMPOSED)
              .streamMode(Archive.StreamMode.AUTO)
              .resolution("1920x1080")
              .multiArchiveTag("MyArchiveTag")
              .build();

        assertNotNull(properties.toMap());

        Archive archive = sdk.startArchive(sessionId, properties);
        assertNotNull(archive);
        assertEquals(sessionId, archive.getSessionId());
        assertNotNull(archive.getId());
        verify(postRequestedFor(urlMatching(archivePath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(archivePath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartArchiveWithScreenshareType() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(archivePath))
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

        String expectedJson = String.format("{\"sessionId\":\"%s\",\"streamMode\":\"auto\",\"hasVideo\":true,\"hasAudio\":true,\"outputMode\":\"composed\",\"layout\":{\"type\":\"bestFit\",\"screenshareType\":\"pip\"}}",sessionId);

        ArchiveLayout layout = new ArchiveLayout(ScreenShareLayoutType.BESTFIT);
        assertEquals(ScreenShareLayoutType.BESTFIT, layout.getScreenshareType());
        layout.setScreenshareType(ScreenShareLayoutType.PIP);
        assertEquals(ScreenShareLayoutType.PIP, layout.getScreenshareType());

        ArchiveProperties properties = new ArchiveProperties.Builder().name(null).layout(layout).build();
        Archive archive = sdk.startArchive(sessionId, properties);
        assertNotNull(archive);
        assertEquals(sessionId, archive.getSessionId());
        assertNotNull(archive.getId());
        verify(postRequestedFor(urlMatching(archivePath)).withRequestBody(equalToJson(expectedJson)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(archivePath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartArchiveWithResolution() throws OpenTokException {
        String sessionId = "SESSIONID";
        ArchiveProperties properties = new ArchiveProperties.Builder().resolution("1280x720").build();
        stubFor(post(urlEqualTo(archivePath))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"createdAt\" : 1395183243556,\n" +
                          "          \"duration\" : 0,\n" +
                          "          \"id\" : \"30b3ebf1-ba36-4f5b-8def-6f70d9986fe9\",\n" +
                          "          \"name\" : \"\",\n" +
                          "          \"resolution\" : \"1280x720\",\n" +
                          "          \"partnerId\" : 123456,\n" +
                          "          \"reason\" : \"\",\n" +
                          "          \"sessionId\" : \"SESSIONID\",\n" +
                          "          \"size\" : 0,\n" +
                          "          \"status\" : \"started\",\n" +
                          "          \"url\" : null\n" +
                          "        }")));
        Archive archive = sdk.startArchive(sessionId, properties);
        assertNotNull(archive);
        assertEquals(sessionId, archive.getSessionId());
        assertEquals(archive.getResolution(), "1280x720");
        verify(postRequestedFor(urlMatching(archivePath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(archivePath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartArchiveWithResolutionInIndividualMode() throws OpenTokException {
        String sessionId = "SESSIONID";
        ArchiveProperties properties = new ArchiveProperties.Builder().outputMode(OutputMode.INDIVIDUAL).resolution("1280x720").build();
        try {
            sdk.startArchive(sessionId, properties);
        } catch (InvalidArgumentException e) {
            assertEquals(e.getMessage(), "The resolution cannot be specified for individual output mode.");
        }
    }

    @Test
    public void testSetArchiveLayoutVertical() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        ArchiveProperties properties = new ArchiveProperties.Builder().layout(new ArchiveLayout(ArchiveLayout.Type.VERTICAL)).build();
        String url = "/v2/project/" + this.apiKey + "/archive/" + archiveId + "/layout";
        stubFor(put(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")));

        sdk.setArchiveLayout(archiveId, properties);
        verify(putRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(putRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testSetArchiveLayoutScreenshareType() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        ArchiveProperties properties = new ArchiveProperties.Builder().layout(new ArchiveLayout(ScreenShareLayoutType.PIP)).build();
        String url = "/v2/project/" + this.apiKey + "/archive/" + archiveId + "/layout";
        stubFor(put(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")));
        String expectedJson = "{\"type\":\"bestFit\",\"screenshareType\":\"pip\"}";
        sdk.setArchiveLayout(archiveId, properties);
        verify(putRequestedFor(urlMatching(url)).withRequestBody(equalToJson(expectedJson)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(putRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testSetArchiveLayoutScreenshareTypeNonBestFitType() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        ArchiveProperties properties = new ArchiveProperties.Builder().layout(new ArchiveLayout(ScreenShareLayoutType.PIP)).build();
        properties.layout().setType(ArchiveLayout.Type.PIP);
        try {
            sdk.setArchiveLayout(archiveId, properties);
            fail("Expected an exception, failing");
        } catch (InvalidArgumentException e) {
            assertEquals("Could not set the Archive layout. When screenshareType is set, type must be bestFit", e.getMessage());
        }
    }

    @Test
    public void testSetArchiveLayoutCustom() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        ArchiveProperties properties = new ArchiveProperties.Builder().layout(new ArchiveLayout(ArchiveLayout.Type.CUSTOM, "stream { position: absolute; }")).build();
        String url = "/v2/project/" + this.apiKey + "/archive/" + archiveId + "/layout";
        stubFor(put(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")));

        sdk.setArchiveLayout(archiveId, properties);
        verify(putRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret, findAll(putRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testSetArchiveLayoutCustomWithNoStyleSheet() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        ArchiveProperties properties = new ArchiveProperties.Builder().layout(new ArchiveLayout(ArchiveLayout.Type.CUSTOM)).build();
        assertThrows(RequestException.class, () -> sdk.setArchiveLayout(archiveId, properties));
    }

    @Test
    public void testSetArchiveLayoutNonCustomWithStyleSheet() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        ArchiveProperties properties = new ArchiveProperties.Builder().layout(new ArchiveLayout(ArchiveLayout.Type.BESTFIT, "stream { position: absolute; }")).build();
        assertThrows(RequestException.class, () -> sdk.setArchiveLayout(archiveId, properties));
    }

    @Test(expected = InvalidArgumentException.class)
    public void testSetArchiveLayoutWithNoProperties() throws OpenTokException {
        sdk.setArchiveLayout("ARCHIVEID", null);
    }

    @Test(expected = InvalidArgumentException.class)
    public void testSetArchiveStreamsLayoutWithNoProps() throws OpenTokException {
        sdk.setStreamLayouts("SESSIONID", null);
    }

    @Test(expected = InvalidArgumentException.class)
    public void testSetArchiveStreamsLayoutWithNoSessionID() throws OpenTokException {
        sdk.setStreamLayouts("", new StreamListProperties.Builder().build());
    }

    @Test
    public void testSetArchiveStreamsMultiLayout() throws OpenTokException {
        String sessionId = "SESSIONID";
        String streamId1 = "STREAMID1";
        String streamId2 = "STREAMID2";
        StreamProperties streamProps1 = new StreamProperties.Builder().id(streamId1).addLayoutClass("full").addLayoutClass("focus").build();
        StreamProperties streamProps2 = new StreamProperties.Builder().id(streamId2).addLayoutClass("full").build();
        StreamListProperties properties = new StreamListProperties.Builder().addStreamProperties(streamProps1).addStreamProperties(streamProps2).build();
        String url = "/v2/project/" + this.apiKey + "/session/" + sessionId + "/stream";
        stubFor(put(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")));
        sdk.setStreamLayouts(sessionId, properties);
        verify(putRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(putRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testSetArchiveStreamsOneLayout() throws OpenTokException {
        String sessionId = "SESSIONID";
        String streamId = "STREAMID1";

        StreamProperties streamProps = new StreamProperties.Builder().id(streamId).addLayoutClass("full").addLayoutClass("focus").build();
        StreamListProperties properties = new StreamListProperties.Builder().addStreamProperties(streamProps).build();
        String url = "/v2/project/" + this.apiKey + "/session/" + sessionId + "/stream";
        stubFor(put(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")));
        sdk.setStreamLayouts(sessionId, properties);
        verify(putRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(putRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testSetArchiveStreamsNoLayout() throws OpenTokException {
        String sessionId = "SESSIONID";
        String streamId = "STREAMID1";

        StreamProperties streamProps = new StreamProperties.Builder().id(streamId).build();
        StreamListProperties properties = new StreamListProperties.Builder().addStreamProperties(streamProps).build();
        String url = "/v2/project/" + this.apiKey + "/session/" + sessionId + "/stream";
        stubFor(put(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")));
        sdk.setStreamLayouts(sessionId, properties);
        verify(putRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(putRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartArchiveWithName() throws OpenTokException {
        String sessionId = "SESSIONID";
        String name = "archive_name";
        stubFor(post(urlEqualTo(archivePath))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"createdAt\" : 1395183243556,\n" +
                          "          \"duration\" : 0,\n" +
                          "          \"id\" : \"30b3ebf1-ba36-4f5b-8def-6f70d9986fe9\",\n" +
                          "          \"name\" : \"archive_name\",\n" +
                          "          \"partnerId\" : 123456,\n" +
                          "          \"reason\" : \"\",\n" +
                          "          \"sessionId\" : \"SESSIONID\",\n" +
                          "          \"size\" : 0,\n" +
                          "          \"status\" : \"started\",\n" +
                          "          \"url\" : null\n" +
                          "        }")));

        Archive archive = sdk.startArchive(sessionId, name);
        assertNotNull(archive);
        assertEquals(sessionId, archive.getSessionId());
        assertEquals(name, archive.getName());
        assertNotNull(archive.getId());
        verify(postRequestedFor(urlMatching(archivePath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(archivePath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartVoiceOnlyArchive() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(archivePath))
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
                          "          \"url\" : null,\n" +
                          "          \"hasVideo\" : false,\n" +
                          "          \"hasAudio\" : true\n" +
                          "        }")));
        ArchiveProperties properties = new ArchiveProperties.Builder().hasVideo(false).build();
        Archive archive = sdk.startArchive(sessionId, properties);
        assertNotNull(archive);
        assertEquals(sessionId, archive.getSessionId());
        assertNotNull(archive.getId());
        verify(postRequestedFor(urlMatching(archivePath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(archivePath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartComposedArchive() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(archivePath))
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
                          "          \"url\" : null,\n" +
                          "          \"outputMode\" : \"composed\"\n" +
                          "        }")));
        ArchiveProperties properties = new ArchiveProperties.Builder()
              .outputMode(OutputMode.COMPOSED)
              .build();
        Archive archive = sdk.startArchive(sessionId, properties);
        assertNotNull(archive);
        assertEquals(sessionId, archive.getSessionId());
        assertNotNull(archive.getId());
        assertEquals(OutputMode.COMPOSED, archive.getOutputMode());
        verify(postRequestedFor(urlMatching(archivePath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(archivePath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartComposedArchiveWithLayout() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(archivePath))
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
                          "          \"url\" : null,\n" +
                          "          \"outputMode\" : \"composed\"\n" +
                          "        }")));
        ArchiveProperties properties = new ArchiveProperties.Builder()
              .outputMode(OutputMode.COMPOSED)
              .layout(new ArchiveLayout(ArchiveLayout.Type.CUSTOM, "stream { position: absolute; }"))
              .build();
        Archive archive = sdk.startArchive(sessionId, properties);
        assertNotNull(archive);
        assertEquals(sessionId, archive.getSessionId());
        assertNotNull(archive.getId());
        assertEquals(OutputMode.COMPOSED, archive.getOutputMode());
        verify(postRequestedFor(urlMatching(archivePath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(archivePath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartArchiveInvalidType() throws OpenTokException {
        String sessionId = "SESSIONID";
        ArchiveProperties properties = new ArchiveProperties.Builder().layout(new ArchiveLayout(ScreenShareLayoutType.PIP)).build();
        properties.layout().setType(ArchiveLayout.Type.PIP);
        try {
            sdk.startArchive(sessionId, properties);
            fail("Expected exception, failing");
        } catch (InvalidArgumentException e) {
            assertEquals("Could not start Archive. When screenshareType is set in the layout, type must be bestFit", e.getMessage());
        }
    }

    @Test
    public void testStartIndividualArchive() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(archivePath))
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
                          "          \"url\" : null,\n" +
                          "          \"outputMode\" : \"individual\"\n" +
                          "        }")));
        ArchiveProperties properties = new ArchiveProperties.Builder().outputMode(OutputMode.INDIVIDUAL).build();
        Archive archive = sdk.startArchive(sessionId, properties);
        assertNotNull(archive);
        assertEquals(sessionId, archive.getSessionId());
        assertNotNull(archive.getId());
        assertEquals(OutputMode.INDIVIDUAL, archive.getOutputMode());
        verify(postRequestedFor(urlMatching(archivePath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(archivePath)))));
        Helpers.verifyUserAgent();
    }

    // TODO: test start archive with name

    // TODO: test start archive failure scenarios

    @Test
    public void testStopArchive() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        stubFor(post(urlEqualTo(archivePath + "/" + archiveId + "/stop"))
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
        verify(postRequestedFor(urlMatching(archivePath + "/" + archiveId + "/stop")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(archivePath + "/" + archiveId + "/stop")))));
        Helpers.verifyUserAgent();
    }

    // TODO: test stop archive failure scenarios

    @Test
    public void testDeleteArchive() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        stubFor(delete(urlEqualTo(archivePath + "/" + archiveId))
              .willReturn(aResponse()
                    .withStatus(204)
                    .withHeader("Content-Type", "application/json")));

        sdk.deleteArchive(archiveId);
        verify(deleteRequestedFor(urlMatching(archivePath + "/" + archiveId)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(deleteRequestedFor(urlMatching(archivePath + "/" + archiveId)))));
        Helpers.verifyUserAgent();
    }

    // TODO: test delete archive failure scenarios

    // NOTE: this test is pretty sloppy
    @Test
    public void testGetExpiredArchive() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        stubFor(get(urlEqualTo(archivePath + "/" + archiveId))
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
                          "          \"status\" : \"expired\",\n" +
                          "          \"url\" : null\n" +
                          "        }")));

        Archive archive = sdk.getArchive(archiveId);
        assertNotNull(archive);
        assertEquals(Archive.Status.EXPIRED, archive.getStatus());
    }

    // NOTE: this test is pretty sloppy
    @Test
    public void testGetPausedArchive() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        stubFor(get(urlEqualTo(archivePath + "/" + archiveId))
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
                          "          \"status\" : \"paused\",\n" +
                          "          \"url\" : null\n" +
                          "        }")));

        Archive archive = sdk.getArchive(archiveId);
        assertNotNull(archive);
        assertEquals(Archive.Status.PAUSED, archive.getStatus());
    }

    @Test
    public void testGetArchiveWithUnknownProperties() throws OpenTokException {
        String archiveId = "ARCHIVEID";
        stubFor(get(urlEqualTo(archivePath + "/" + archiveId))
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
                          "          \"status\" : \"expired\",\n" +
                          "          \"url\" : null,\n" +
                          "          \"thisisnotaproperty\" : null\n" +
                          "        }")));

        Archive archive = sdk.getArchive(archiveId);
        assertNotNull(archive);
    }

    @Test
    public void testGetStreamWithId() throws OpenTokException {
        String sessionID = "SESSIONID";
        String streamID = "STREAMID";
        String url = "/v2/project/" + this.apiKey + "/session/" + sessionID + "/stream/" + streamID;
        stubFor(get(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"id\" : \"" + streamID + "\",\n" +
                          "          \"name\" : \"\",\n" +
                          "          \"videoType\" : \"camera\",\n" +
                          "          \"layoutClassList\" : [] \n" +
                          "        }")));
        Stream stream = sdk.getStream(sessionID, streamID);
        assertNotNull(stream);
        assertEquals(streamID, stream.getId());
        assertEquals("", stream.getName());
        assertEquals("camera", stream.getVideoType());

        verify(getRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(getRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testForceMuteStream() throws OpenTokException {
        String sessionID = "SESSIONID";
        String streamID = "STREAMID";
        String path = "/v2/project/" + this.apiKey + "/session/" + sessionID + "/stream/" + streamID + "/mute";
        stubFor(post(urlEqualTo(path))
              .willReturn(aResponse().withStatus(200)));
        sdk.forceMuteStream(sessionID, streamID);
        verify(postRequestedFor(urlMatching(path)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret, findAll(postRequestedFor(urlMatching(path)))));
        Helpers.verifyUserAgent();
        assertThrows(InvalidArgumentException.class, () -> sdk.forceMuteStream("", streamID));
        assertThrows(InvalidArgumentException.class, () -> sdk.forceMuteStream(sessionID, ""));
    }

    @Test
    public void testForceMuteAllStreamWithIdList() throws OpenTokException {
        String sessionID = "SESSIONID";
        String path = "/v2/project/" + this.apiKey + "/session/" + sessionID + "/mute";
        stubFor(post(urlEqualTo(path))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")));
        List<String> excludedList = new ArrayList<>();
        excludedList.add("abc123");
        excludedList.add("xyz456");
        MuteAllProperties properties = new MuteAllProperties.Builder()
              .excludedStreamIds(excludedList).build();
        sdk.forceMuteAll(sessionID, properties);
        verify(postRequestedFor(urlMatching(path)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(SESSION_CREATE)))));
        Helpers.verifyUserAgent();
        assertThrows(InvalidArgumentException.class, () -> sdk.forceMuteAll("", properties));
    }

    @Test
    public void testDisableForceMute() throws OpenTokException {
        String sessionID = "SESSIONID";
        String path = "/v2/project/" + this.apiKey + "/session/" + sessionID + "/mute";
        stubFor(post(urlEqualTo(path))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")));
        sdk.disableForceMute(sessionID);
        verify(postRequestedFor(urlMatching(path)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(SESSION_CREATE)))));
        Helpers.verifyUserAgent();
        assertThrows(InvalidArgumentException.class, () -> sdk.disableForceMute(""));
    }

    @Test
    public void testListStreams() throws OpenTokException {
        String sessionID = "SESSIONID";
        String url = "/v2/project/" + this.apiKey + "/session/" + sessionID + "/stream";
        stubFor(get(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"count\" : 2,\n" +
                          "          \"items\" : [ {\n" +
                          "          \"id\" : \"" + 1234 + "\",\n" +
                          "          \"name\" : \"\",\n" +
                          "          \"videoType\" : \"camera\",\n" +
                          "          \"layoutClassList\" : [] \n" +
                          "          }, {\n" +
                          "          \"id\" : \"" + 5678 + "\",\n" +
                          "          \"name\" : \"\",\n" +
                          "          \"videoType\" : \"screen\",\n" +
                          "          \"layoutClassList\" : [] \n" +
                          "          } ]\n" +
                          "        }")));
        StreamList streams = sdk.listStreams(sessionID);
        assertNotNull(streams);
        assertEquals(2, streams.getTotalCount());
        Stream stream1 = streams.get(0);
        Stream stream2 = streams.get(1);
        assertEquals("1234", stream1.getId());
        assertEquals("", stream1.getName());
        assertEquals("camera", stream1.getVideoType());
        assertEquals("5678", stream2.getId());
        assertEquals("", stream2.getName());
        assertEquals("screen", stream2.getVideoType());

        verify(getRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(getRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartBroadcastNullEmptyParameters() throws OpenTokException {
        int exceptionCount = 0;
        BroadcastProperties properties = new BroadcastProperties.Builder().build();
        try {
            sdk.startBroadcast("", properties);
        } catch (InvalidArgumentException e) {
            exceptionCount++;
        }
        try {
            sdk.startBroadcast(null, properties);
        } catch (InvalidArgumentException e) {
            exceptionCount++;
        }
        try {
            sdk.startBroadcast("SESSIONID", null);
        } catch (InvalidArgumentException e) {
            exceptionCount++;
        }
        assertEquals(3, exceptionCount);
    }

    @Test
    public void testStartBroadcast() throws OpenTokException {
        String sessionId = "SESSIONID";
        String url = "/v2/project/" + this.apiKey + "/broadcast";
        stubFor(post(urlEqualTo(url))
              .withRequestBody(equalTo("{\"sessionId\":\"SESSIONID\",\"streamMode\":\"auto\"," +
                    "\"hasAudio\":false,\"hasVideo\":false,\"layout\":{\"type\":\"pip\"},\"maxDuration\":1000," +
                    "\"maxBitrate\":524288,\"resolution\":\"1920x1080\",\"multiBroadcastTag\":\"MyVideoBroadcastTag\"," +
                    "\"outputs\":{\"hls\":{},\"rtmp\":[{\"id\":\"foo\",\"serverUrl\":\"rtmp://myfooserver/myfooapp\"," +
                    "\"streamName\":\"myfoostream\"},{\"id\":\"bar\",\"serverUrl\":" +
                    "\"rtmp://mybarserver/mybarapp\",\"streamName\":\"mybarstream\"}]}}"
              ))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"id\" : \"30b3ebf1-ba36-4f5b-8def-6f70d9986fe9\",\n" +
                          "          \"sessionId\" : \"SESSIONID\",\n" +
                          "          \"projectId\" : 123456,\n" +
                          "          \"createdAt\" : 1437676551000,\n" +
                          "          \"updatedAt\" : 1447676551000,\n" +
                          "          \"maxDuration\": 5400,\n" +
                          "          \"maxBitrate\": 7234560,\n" +
                          "          \"hasAudio\" : false,\n" +
                          "          \"hasVideo\" : false,\n" +
                          "          \"resolution\" : \"1280x720\",\n" +
                          "          \"status\" : \"started\",\n" +
                          "          \"multiBroadcastTag\" : \"MyVideoBroadcastTag\",\n" +
                          "          \"broadcastUrls\" : {" +
                          "           \"hls\" : \"http://server/fakepath/playlist.m3u8\"," +
                          "           \"hlsStatus\" : \"ready\"," +
                          "           \"rtmp\" : [{" +
                          "           \"id\" : \"foo\"," +
                          "           \"serverUrl\" : \"rtmp://myfooserver/myfooapp\"," +
                          "           \"streamName\" : \"myfoostream\"," +
                          "           \"status\" : \"live\"" +
                          "           },{" +
                          "           \"id\" : \"bar\"," +
                          "           \"serverUrl\" : \"rtmp://mybarserver/mybarapp\"," +
                          "           \"streamName\" : \"mybarstream\"," +
                          "           \"status\" : \"offline\"" +
                          "           }]" +
                          "           }" +
                          "           }" +
                          "        }")));
        RtmpProperties rtmpProps = new RtmpProperties.Builder().id("foo").serverUrl("rtmp://myfooserver/myfooapp").streamName("myfoostream").build();
        RtmpProperties rtmpNextProps = new RtmpProperties.Builder().id("bar").serverUrl("rtmp://mybarserver/mybarapp").streamName("mybarstream").build();
        BroadcastLayout layout = new BroadcastLayout(BroadcastLayout.Type.PIP);
        new BroadcastLayout(ArchiveLayout.Type.BESTFIT, "style.css");

        BroadcastProperties properties = new BroadcastProperties.Builder()
              .hasHls(true)
              .addRtmpProperties(rtmpProps)
              .addRtmpProperties(rtmpNextProps)
              .maxDuration(1000)
              .maxBitrate(524288)
              .resolution("1920x1080")
              .hasAudio(false).hasVideo(false)
              .multiBroadcastTag("MyVideoBroadcastTag")
              .layout(layout)
              .streamMode(Broadcast.StreamMode.AUTO)
              .build();

        Broadcast broadcast = sdk.startBroadcast(sessionId, properties);
        assertNotNull(broadcast);
        Rtmp rtmp = broadcast.getRtmpList().get(0);
        assertNotNull(rtmp);
        assertNotNull(rtmp.getId());
        assertNotNull(rtmp.getServerUrl());
        assertNotNull(rtmp.getStreamName());
        assertNotNull(broadcast.toString());
        assertEquals("started", broadcast.getStatus());
        assertFalse(broadcast.hasAudio());
        assertFalse(broadcast.hasVideo());
        assertEquals(7234560, broadcast.getMaxBitrate());
        assertEquals(5400, broadcast.getMaxDuration());
        assertEquals("1280x720", broadcast.getResolution());
        assertEquals(1437676551000L, broadcast.getCreatedAt());
        assertEquals(1447676551000L, broadcast.getUpdatedAt());
        assertEquals(123456, broadcast.getProjectId());
        assertEquals(sessionId, broadcast.getSessionId());
        assertEquals("MyVideoBroadcastTag", broadcast.getMultiBroadcastTag());
        assertEquals(Broadcast.StreamMode.AUTO, broadcast.getStreamMode());
        assertNotNull(broadcast.getId());
        verify(postRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testBroadcastHlsBuilder() {
        assertThrows(IllegalArgumentException.class, () ->
              new Hls.Builder().lowLatency(true).dvr(true).build()
        );
        new Hls.Builder().lowLatency(true).dvr(false).build();
        new Hls.Builder().lowLatency(false).dvr(true).build();
        Hls hls = new Hls.Builder().build();
        assertFalse(hls.dvr());
        assertFalse(hls.lowLatency());
    }

    @Test
    public void testStartBroadcastHlsParameters() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(broadcastPath))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"id\" : \"30b3ebf1-ba36-4f5b-8def-6f70d9986fe9\",\n" +
                          "          \"sessionId\" : \"SESSIONID\",\n" +
                          "          \"projectId\" : 123456,\n" +
                          "          \"createdAt\" : 1437676551000,\n" +
                          "          \"upDatedAt\" : 1437676551000,\n" +
                          "          \"resolution\" : \"1280x720\",\n" +
                          "          \"status\" : \"started\",\n" +
                          "          \"broadcastUrls\" : {" +
                          "           \"hls\" : \"http://server/fakepath/playlist.m3u8\"," +
                          "           \"rtmp\" : [{" +
                          "           \"id\" : \"foo\"," +
                          "           \"serverUrl\" : \"rtmp://myfooserver/myfooapp\"," +
                          "           \"streamName\" : \"myfoostream\"" +
                          "           }," +
                          "           {                          " +
                          "           \"id\" : \"bar\"," +
                          "           \"serverUrl\" : \"rtmp://mybarserver/mybarapp\"," +
                          "           \"streamName\" : \"mybarstream\"" +
                          "           }]" +
                          "           }" +
                          "           }" +
                          "        }")));
        RtmpProperties rtmpProps = new RtmpProperties.Builder().id("foo").serverUrl("rtmp://myfooserver/myfooapp").streamName("myfoostream").build();
        RtmpProperties rtmpNextProps = new RtmpProperties.Builder().id("bar").serverUrl("rtmp://mybarserver/mybarapp").streamName("mybarstream").build();
        BroadcastLayout layout = new BroadcastLayout(BroadcastLayout.Type.BESTFIT);
        BroadcastProperties properties = new BroadcastProperties.Builder()
              .hls(new Hls.Builder().lowLatency(true).build())
              .addRtmpProperties(rtmpProps)
              .addRtmpProperties(rtmpNextProps)
              .maxDuration(1000)
              .resolution("1920x1080")
              .layout(layout)
              .build();
        Broadcast broadcast = sdk.startBroadcast(sessionId, properties);
        verify(postRequestedFor(urlMatching(broadcastPath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(broadcastPath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testBroadcastWithScreenShareType() throws OpenTokException {
        String sessionId = "2_M23039383dlkeoedjd-22928edjdHKUiuhkfofoieo98899imf-fg";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode broadcastRootNode = mapper.createObjectNode();
        broadcastRootNode.put("id", "35c4596f-f92a-465b-b319-828d3de87b949");
        broadcastRootNode.put("sessionId", sessionId);
        broadcastRootNode.put("projectId", "87654321");
        broadcastRootNode.put("createdAt", "1437676551000");
        ObjectNode broadcastUrlNode = mapper.createObjectNode();
        broadcastUrlNode.put("hls", "http://server/fakepath/playlist.m3u8");
        broadcastRootNode.set("broadcastUrls", broadcastUrlNode);
        broadcastRootNode.put("updatedAt", "1437676551000");
        broadcastRootNode.put("status", "started");
        broadcastRootNode.put("maxDuration", "5400");
        broadcastRootNode.put("maxBitrate", "2000000");
        broadcastRootNode.put("resolution", "1280x720");
        broadcastRootNode.put("partnerId", "12345678");
        broadcastRootNode.put("event", "broadcast");

        stubFor(post(urlEqualTo(broadcastPath))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(broadcastRootNode.toString())));

        BroadcastLayout layout = new BroadcastLayout(ScreenShareLayoutType.PIP);

        BroadcastProperties properties = new BroadcastProperties.Builder()
                .hasHls(true)
                .maxDuration(5400)
                .layout(layout)
                .build();
        String expectedJson = String.format("{\"sessionId\":\"%s\",\"streamMode\":\"auto\",\"hasAudio\":true,\"hasVideo\":true,\"layout\":{\"type\":\"bestFit\",\"screenshareType\":\"pip\"},\"maxDuration\":5400,\"maxBitrate\":2000000,\"resolution\":\"640x480\",\"outputs\":{\"hls\":{},\"rtmp\":[]}}",sessionId);
        Broadcast broadcast = sdk.startBroadcast(sessionId, properties);
        assertNotNull(broadcast);
        assertEquals(sessionId, broadcast.getSessionId());
        assertNotNull(broadcast.getId());
        verify(postRequestedFor(urlMatching(broadcastPath)).withRequestBody(equalToJson(expectedJson)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret, findAll(postRequestedFor(urlMatching(broadcastPath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartBroadcastWithScreenshareAndInvalidType() throws OpenTokException {
        String sessionId = "2_M23039383dlkeoedjd-22928edjdHKUiuhkfofoieo98899imf-fg";
        BroadcastProperties properties = new BroadcastProperties.Builder().layout(new BroadcastLayout(ScreenShareLayoutType.PIP)).build();
        properties.layout().setType(ArchiveLayout.Type.PIP);
        try {
            sdk.startBroadcast(sessionId, properties);
            assertTrue("Expected exception - failing", true);
        } catch (InvalidArgumentException e) {
            assertEquals("Could not start OpenTok Broadcast, Layout Type must be bestfit when screenshareType is set.", e.getMessage());
        }
    }

    @Test
    public void testStartBroadcastWithCustomLayout() throws OpenTokException {
        String sessionId = "2_M23039383dlkeoedjd-22928edjdHKUiuhkfofoieo98899imf-fg";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode broadcastRootNode = mapper.createObjectNode();
        broadcastRootNode.put("id", "35c4596f-f92a-465b-b319-828d3de87b949");
        broadcastRootNode.put("sessionId", sessionId);
        broadcastRootNode.put("projectId", "87654321");
        broadcastRootNode.put("createdAt", "1437676551000");

        ObjectNode broadcastUrlNode = mapper.createObjectNode();
        broadcastUrlNode.put("hls", "http://server/fakepath/playlist.m3u8");

        broadcastRootNode.set("broadcastUrls", broadcastUrlNode);
        broadcastRootNode.put("updatedAt", "1437676551000");
        broadcastRootNode.put("status", "started");
        broadcastRootNode.put("maxDuration", "5400");
        broadcastRootNode.put("resolution", "1280x720");
        broadcastRootNode.put("partnerId", "12345678");
        broadcastRootNode.put("event", "broadcast");


        stubFor(post(urlEqualTo(broadcastPath))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(broadcastRootNode.toString())));

        BroadcastLayout layout = new BroadcastLayout(BroadcastLayout.Type.CUSTOM);
        String customStylesheet = "stream.instructor {position: absolute; width: 100%;  height:50%;}";
        layout.setStylesheet(customStylesheet);

        BroadcastProperties properties = new BroadcastProperties.Builder()
              .hasHls(true)
              .maxDuration(5400)
              .layout(layout)
              .build();
        Broadcast broadcast = sdk.startBroadcast(sessionId, properties);
        assertNotNull(broadcast);
        assertEquals(sessionId, broadcast.getSessionId());
        assertNotNull(broadcast.getId());
        verify(postRequestedFor(urlMatching(broadcastPath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(broadcastPath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartBroadcastNoHls() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(broadcastPath))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"id\" : \"30b3ebf1-ba36-4f5b-8def-6f70d9986fe9\",\n" +
                          "          \"sessionId\" : \"SESSIONID\",\n" +
                          "          \"projectId\" : 123456,\n" +
                          "          \"createdAt\" : 1437676551000,\n" +
                          "          \"upDatedAt\" : 1437676551000,\n" +
                          "          \"resolution\" : \"1280x720\",\n" +
                          "          \"status\" : \"started\",\n" +
                          "          \"broadcastUrls\" : {" +
                          "           \"rtmp\" : [{" +
                          "           \"id\" : \"foo\"," +
                          "           \"serverUrl\" : \"rtmp://myfooserver/myfooapp\"," +
                          "           \"streamName\" : \"myfoostream\"" +
                          "           }," +
                          "           {                          " +
                          "           \"id\" : \"bar\"," +
                          "           \"serverUrl\" : \"rtmp://mybarserver/mybarapp\"," +
                          "           \"streamName\" : \"mybarstream\"" +
                          "           }]" +
                          "           }" +
                          "        }")));
        RtmpProperties rtmpProps = new RtmpProperties.Builder().id("foo").serverUrl("rtmp://myfooserver/myfooapp").streamName("myfoostream").build();
        RtmpProperties rtmpNextProps = new RtmpProperties.Builder().id("bar").serverUrl("rtmp://mybarserver/mybarapp").streamName("mybarstream").build();
        BroadcastLayout layout = new BroadcastLayout(BroadcastLayout.Type.PIP);
        BroadcastProperties properties = new BroadcastProperties.Builder()
              .addRtmpProperties(rtmpProps)
              .addRtmpProperties(rtmpNextProps)
              .maxDuration(1000)
              .resolution("640x480")
              .layout(layout)
              .build();
        Broadcast broadcast = sdk.startBroadcast(sessionId, properties);
        assertNotNull(broadcast);
        assertEquals(sessionId, broadcast.getSessionId());
        assertNotNull(broadcast.getId());
        assertNull(broadcast.getHls());
        verify(postRequestedFor(urlMatching(broadcastPath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(broadcastPath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartBroadcastNoRtmp() throws OpenTokException {
        String sessionId = "SESSIONID";
        stubFor(post(urlEqualTo(broadcastPath))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"id\" : \"30b3ebf1-ba36-4f5b-8def-6f70d9986fe9\",\n" +
                          "          \"sessionId\" : \"SESSIONID\",\n" +
                          "          \"projectId\" : 123456,\n" +
                          "          \"createdAt\" : 1437676551000,\n" +
                          "          \"upDatedAt\" : 1437676551000,\n" +
                          "          \"resolution\" : \"1280x720\",\n" +
                          "          \"status\" : \"started\",\n" +
                          "          \"broadcastUrls\" : {" +
                          "           \"hls\" : \"http://server/fakepath/playlist.m3u8\"" +
                          "           }" +
                          "        }")));
        RtmpProperties rtmpProps = new RtmpProperties.Builder().id("foo").serverUrl("rtmp://myfooserver/myfooapp").streamName("myfoostream").build();
        RtmpProperties rtmpNextProps = new RtmpProperties.Builder().id("bar").serverUrl("rtmp://mybarserver/mybarapp").streamName("mybarstream").build();
        BroadcastLayout layout = new BroadcastLayout(BroadcastLayout.Type.PIP);
        BroadcastProperties properties = new BroadcastProperties.Builder()
              .addRtmpProperties(rtmpProps)
              .addRtmpProperties(rtmpNextProps)
              .maxDuration(1000)
              .resolution("640x480")
              .layout(layout)
              .build();
        Broadcast broadcast = sdk.startBroadcast(sessionId, properties);
        assertNotNull(broadcast);
        assertEquals(sessionId, broadcast.getSessionId());
        assertNotNull(broadcast.getId());
        assertTrue(broadcast.getRtmpList().isEmpty());
        verify(postRequestedFor(urlMatching(broadcastPath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(broadcastPath)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testBroadcastPropertiesWithSixRtmp() throws OpenTokException {
        boolean caughtException = false;
        try {
            RtmpProperties rtmpProps = new RtmpProperties.Builder().id("foo").serverUrl("rtmp://myfooserver/myfooapp").streamName("myfoostream").build();
            RtmpProperties rtmpNextProps = new RtmpProperties.Builder().id("bar").serverUrl("rtmp://mybarserver/mybarapp").streamName("mybarstream").build();
            new BroadcastProperties.Builder()
                  .addRtmpProperties(rtmpProps)
                  .addRtmpProperties(rtmpNextProps)
                  .addRtmpProperties(rtmpProps)
                  .addRtmpProperties(rtmpNextProps)
                  .addRtmpProperties(rtmpProps)
                  .addRtmpProperties(rtmpNextProps)
                  .build();
        } catch (InvalidArgumentException e) {
            caughtException = true;
        }
        assertTrue(caughtException);
    }

    @Test
    public void testPatchBroadcast() throws OpenTokException {
        String broadcastId = "BROADCASTID";
        String streamId = "abc123efg456";
        stubFor(patch(urlEqualTo(broadcastPath + "/" + broadcastId + "/streams"))
              .willReturn(aResponse().withStatus(204)));
        sdk.addBroadcastStream(broadcastId, streamId, true, true);
        verify(patchRequestedFor(urlMatching(broadcastPath + "/" + broadcastId + "/streams")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(deleteRequestedFor(urlMatching(archivePath + "/" + broadcastId)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testPatchBroadcastExpectException() throws OpenTokException {
        String broadcastId = "BROADCASTID";
        Exception exception = assertThrows(OpenTokException.class, () -> sdk.removeBroadcastStream(broadcastId, ""));
        String got = exception.getMessage();
        String expected = "Could not patch broadcast, needs one of: addStream or removeStream";
        assertEquals(expected, got);
    }

    @Test
    public void testStopBroadcast() throws OpenTokException {
        String broadcastId = "BROADCASTID";
        String url = "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId + "/stop";
        stubFor(post(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"id\" :  \"" + broadcastId + "\",\n" +
                          "          \"sessionId\" : \"SESSIONID\",\n" +
                          "          \"projectId\" : 123456,\n" +
                          "          \"createdAt\" : 1437676551000,\n" +
                          "          \"updatedAt\" : 1437676551000,\n" +
                          "          \"resolution\" : \"1280x720\",\n" +
                          "          \"broadcastUrls\" : null" +
                          "        }")));
        Broadcast broadcast = sdk.stopBroadcast(broadcastId);
        assertNotNull(broadcast);
        assertEquals(broadcastId, broadcast.getId());
        verify(postRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testBroadcastStreamInfo() throws OpenTokException {
        String broadcastId = "BROADCASTID";
        String url = "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId;
        stubFor(get(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"id\" :  \"" + broadcastId + "\",\n" +
                          "          \"sessionId\" : \"SESSIONID\",\n" +
                          "          \"projectId\" : 123456,\n" +
                          "          \"createdAt\" : 1437676551000,\n" +
                          "          \"upDatedAt\" : 1437676551000,\n" +
                          "          \"resolution\" : \"1280x720\",\n" +
                          "          \"status\" : \"started\",\n" +
                          "          \"broadcastUrls\" : {" +
                          "           \"hls\" : \"http://server/fakepath/playlist.m3u8\"," +
                          "           \"rtmp\" : [{" +
                          "           \"id\" : \"foo\"," +
                          "           \"serverUrl\" : \"rtmp://myfooserver/myfooapp\"," +
                          "           \"streamName\" : \"myfoostream\"" +
                          "           }," +
                          "           {                          " +
                          "           \"id\" : \"bar\"," +
                          "           \"serverUrl\" : \"rtmp://mybarserver/mybarapp\"," +
                          "           \"streamName\" : \"mybarstream\"" +
                          "           }]" +
                          "           }" +
                          "           }" +
                          "        }")));
        Broadcast broadcast = sdk.getBroadcast(broadcastId);
        assertNotNull(broadcast);
        assertEquals(broadcastId, broadcast.getId());
        assertEquals("http://server/fakepath/playlist.m3u8", broadcast.getHls());
        assertEquals(2, broadcast.getRtmpList().size());
        verify(getRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
        assertThrows(InvalidArgumentException.class, () -> sdk.getBroadcast(""));
    }

    @Test
    public void testSetBroadcastLayoutVertical() throws OpenTokException {
        String broadcastId = "BROADCASTID";
        BroadcastProperties properties = new BroadcastProperties.Builder().layout(new BroadcastLayout(BroadcastLayout.Type.VERTICAL)).build();
        String url = "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId + "/layout";
        stubFor(put(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")));

        sdk.setBroadcastLayout(broadcastId, properties);
        verify(putRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(putRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testSetBroadcastLayoutWithScreenshareType() throws OpenTokException {
        String broadcastId = "BROADCASTID";
        BroadcastProperties properties = new BroadcastProperties.Builder().layout(new BroadcastLayout(ScreenShareLayoutType.PIP)).build();
        String url = "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId + "/layout";
        stubFor(put(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")));

        String expectedJson = "{\"type\":\"bestFit\",\"screenshareType\":\"pip\"}";
        sdk.setBroadcastLayout(broadcastId, properties);
        verify(putRequestedFor(urlMatching(url)).withRequestBody(equalToJson(expectedJson)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(putRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testSetBroadcastLayoutWithScreenshareTypeInvalidType() throws OpenTokException {
        String broadcastId = "BROADCASTID";
        BroadcastProperties properties = new BroadcastProperties.Builder().layout(new BroadcastLayout(ScreenShareLayoutType.PIP)).build();
        properties.layout().setType(ArchiveLayout.Type.PIP);
        try {
            sdk.setBroadcastLayout(broadcastId, properties);
            fail("Expected and exception - failing");
        } catch (InvalidArgumentException e) {
            assertEquals("Could not set layout. Type must be bestfit when screenshareLayout is set.", e.getMessage());
        }
    }

    @Test
    public void testSipDialWithEmptyNullParams() throws OpenTokException {
        int exceptionCaughtCount = 0;
        SipProperties properties = new SipProperties.Builder()
              .sipUri("sip:user@sip.partner.com;transport=tls")
              .userName("username")
              .password("password")
              .build();
        try {
            sdk.dial("", "TOKEN", properties);
        } catch (InvalidArgumentException e) {
            exceptionCaughtCount++;
        }
        try {
            sdk.dial(null, "TOKEN", properties);
        } catch (InvalidArgumentException e) {
            exceptionCaughtCount++;
        }
        try {
            sdk.dial("SESSIONID", "", properties);
        } catch (InvalidArgumentException e) {
            exceptionCaughtCount++;
        }
        try {
            sdk.dial("SESSIONID", null, properties);
        } catch (InvalidArgumentException e) {
            exceptionCaughtCount++;
        }
        try {
            sdk.dial("SESSIONID", "TOKEN", null);
        } catch (InvalidArgumentException e) {
            exceptionCaughtCount++;
        }
        properties = new SipProperties.Builder()
              .userName("username")
              .password("password")
              .build();
        try {
            sdk.dial("SESSIONID", "TOKEN", properties);
        } catch (InvalidArgumentException e) {
            exceptionCaughtCount++;
        }
        assertEquals(6, exceptionCaughtCount);
    }

    @Test
    public void testSipDial() throws OpenTokException {
        String sessionId = "SESSIONID";
        String token = "TOKEN";
        String url = "/v2/project/" + this.apiKey + "/dial";
        stubFor(post(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "          \"id\" : \"30b3ebf1-ba36-4f5b-8def-6f70d9986fe9\",\n" +
                          "          \"connectionId\" : \"30b3ebf1-ba36-4f5b-8def-6f70d9986fe9\",\n" +
                          "          \"streamId\" : \"30b3ebf1-ba36-4f5b-8def-6f70d9986fe9\"\n" +
                          "        }")));
        Character dQuote = '"';
        String headerKey = dQuote + "X-someKey" + dQuote;
        String headerValue = dQuote + "someValue" + dQuote;
        String headerJson = "{" + headerKey + ": " + headerValue + "}";
        SipProperties properties = new SipProperties.Builder()
              .sipUri("sip:user@sip.partner.com;transport=tls")
              .from("from@example.com")
              .headersJsonStartingWithXDash(headerJson)
              .userName("username")
              .password("password")
              .secure(true)
              .video(true)
              .streams("Stream ID 1", "STREAM_ID2")
              .observeForceMute(true)
              .build();
        Sip sip = sdk.dial(sessionId, token, properties);
        assertNotNull(sip);
        assertNotNull(sip.getId());
        assertNotNull(sip.getConnectionId());
        assertNotNull(sip.getStreamId());
        verify(postRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testPlayDtmfAll() throws OpenTokException {
        String sessionId = "SESSIONID";
        String path = "/v2/project/" + apiKey + "/session/" + sessionId + "/play-dtmf";
        stubFor(post(urlEqualTo(path))
              .willReturn(aResponse()
                    .withStatus(200)));

        String dtmfString = "0p6p4p4pp60p#";

        sdk.playDTMF(sessionId, dtmfString);
        verify(postRequestedFor(urlMatching(path)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(deleteRequestedFor(urlMatching(path)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testPlayDtmfSingle() throws OpenTokException {
        String sessionId = "SESSIONID";
        String connectionId = "CONNECTIONID";
        String path = "/v2/project/" + apiKey + "/session/" + sessionId +
              "/connection/" + connectionId +"/play-dtmf";
        stubFor(post(urlEqualTo(path))
              .willReturn(aResponse()
                    .withStatus(200)));

        String dtmfString = "0p6p4p4pp60p#";

        sdk.playDTMF(sessionId, connectionId, dtmfString);
        verify(postRequestedFor(urlMatching(path)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(deleteRequestedFor(urlMatching(path)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testForceDisconnect() throws OpenTokException {
        String sessionId = "SESSIONID";
        String connectionId = "CONNECTIONID";
        String path = "/v2/project/" + apiKey + "/session/" + sessionId + "/connection/" + connectionId;
        stubFor(delete(urlEqualTo(path))
              .willReturn(aResponse()
                    .withStatus(204)
                    .withHeader("Content-Type", "application/json")));
        sdk.forceDisconnect(sessionId, connectionId);
        verify(deleteRequestedFor(urlMatching(path)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(deleteRequestedFor(urlMatching(path)))));
        Helpers.verifyUserAgent();
        assertThrows(InvalidArgumentException.class, () -> sdk.forceDisconnect("", connectionId));
        assertThrows(InvalidArgumentException.class, () -> sdk.forceDisconnect(sessionId, ""));
    }

    @Test
    public void testCreateSessionWithProxy() throws OpenTokException, UnknownHostException {
        WireMockConfiguration proxyConfig = WireMockConfiguration.wireMockConfig();
        proxyConfig.dynamicPort();
        WireMockServer proxyingService = new WireMockServer(proxyConfig);
        proxyingService.start();
        WireMock proxyingServiceAdmin = new WireMock(proxyingService.port());

        String targetServiceBaseUrl = "http://localhost:" + wireMockRule.port();
        proxyingServiceAdmin.register(any(urlMatching(".*")).atPriority(10)
              .willReturn(aResponse()
                    .proxiedFrom(targetServiceBaseUrl)));

        String sessionId = "SESSIONID";
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getLocalHost(), proxyingService.port()));
        sdk = new OpenTok.Builder(apiKey, apiSecret).apiUrl(targetServiceBaseUrl).requestTimeout(10).proxy(proxy).build();
        stubFor(post(urlEqualTo("/session/create"))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[{\"session_id\":\"" + sessionId + "\",\"project_id\":\"00000000\"," +
                          "\"partner_id\":\"123456\"," +
                          "\"create_dt\":\"Mon Mar 17 00:41:31 PDT 2014\"," +
                          "\"media_server_url\":\"\"}]")));

        Session session = sdk.createSession();

        assertNotNull(session);
        assertEquals(apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());

        verify(postRequestedFor(urlMatching(SESSION_CREATE)));

        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(SESSION_CREATE)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStartRender() throws Exception {
        String sessionId = "1_MX4yNzA4NjYxMn5-MTU0NzA4MDUyMTEzNn5sOXU5ZnlWYXplRnZGblV4RUo3dXJpZk1-fg";
        String token = "TOKEN";
        String url = "/v2/project/" + this.apiKey + "/render";
        stubFor(post(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(202)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "  \"id\":\"80abaf0d-25a3-4efc-968f-6268d620668d\",\n" +
                          "  \"sessionId\":\""+sessionId+"\",\n" +
                          "  \"projectId\":\"27086612\",\n" +
                          "  \"createdAt\":1547080532099,\n" +
                          "  \"updatedAt\":1547080532199,\n" +
                          "  \"url\": \"https://webapp.customer.com\",\n" +
                          "  \"resolution\": \"480x640\",\n" +
                          "  \"status\":\"starting\",\n" +
                          "  \"streamId\":\"e32445b743678c98230f238\"\n" +
                          "}"
                    )
              )
        );

        RenderProperties properties = new RenderProperties.Builder()
              .url("https://example.com/main")
              .maxDuration(1800)
              .properties(new RenderProperties.Properties("Composed stream for Live event #1"))
              .resolution(RenderProperties.Resolution.SD_VERTICAL)
              .build();

        Render render = sdk.startRender(sessionId, token, properties);
        assertNotNull(render);
        assertEquals("80abaf0d-25a3-4efc-968f-6268d620668d", render.getId());
        assertEquals(sessionId, render.getSessionId());
        assertEquals("27086612", render.getProjectId());
        assertEquals(1547080532099L, render.getCreatedAt());
        assertEquals(1547080532199L, render.getUpdatedAt());
        assertEquals("https://webapp.customer.com", render.getUrl());
        assertEquals("480x640", render.getResolution());
        assertEquals(RenderStatus.STARTING, render.getStatus());
        assertEquals("e32445b743678c98230f238", render.getStreamId());
        assertNull(render.getReason());

        verify(postRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(postRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();

        assertThrows(InvalidArgumentException.class, () -> sdk.startRender("", token, properties));
    }

    @Test
    public void testGetRender() throws Exception {
        String renderId = "80abaf0d-25a3-4efc-968f-6268d620668d";
        String url = "/v2/project/" + this.apiKey + "/render/" + renderId;
        stubFor(get(urlEqualTo(url))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "  \"id\":\""+renderId+"\",\n" +
                          "  \"sessionId\":\"SESSION_ID\",\n" +
                          "  \"projectId\":\"27086612\",\n" +
                          "  \"createdAt\":1547080532099,\n" +
                          "  \"updatedAt\":1547080532199,\n" +
                          "  \"url\": \"https://webapp.customer.com\",\n" +
                          "  \"resolution\": \"480x640\",\n" +
                          "  \"status\":\"failed\",\n" +
                          "  \"reason\":\"Could not load URL\"\n" +
                          "}"
                    )
              )
        );

        Render render = sdk.getRender(renderId);
        assertNotNull(render);
        assertEquals(renderId, render.getId());
        assertEquals("SESSION_ID", render.getSessionId());
        assertEquals("27086612", render.getProjectId());
        assertEquals(1547080532099L, render.getCreatedAt());
        assertEquals(1547080532199L, render.getUpdatedAt());
        assertEquals("https://webapp.customer.com", render.getUrl());
        assertEquals("480x640", render.getResolution());
        assertEquals(RenderStatus.FAILED, render.getStatus());
        assertEquals("Could not load URL", render.getReason());
        assertNull(render.getStreamId());

        verify(getRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(getRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testStopRender() throws Exception {
        String renderId = "10abaf0d-25a3-4efc-968f-6268d620668d";
        String url = "/v2/project/" + this.apiKey + "/render/" + renderId;
        stubFor(delete(urlEqualTo(url)).willReturn(aResponse().withStatus(200)));

        sdk.stopRender(renderId);

        verify(deleteRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(deleteRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testListRenders() throws Exception {
        String sessionId = "SESSION_ID";
        String id = "d95f6496-df6e-4f49-86d6-832e00303602";
        String projectId = "27086612";
        long createdAt = 1547080532099L, updatedAt = 1547080532099L;
        String url = "https://webapp.customer.com";
        String resolution = "1280x720";
        RenderStatus status = RenderStatus.STOPPED;
        String streamId = "d2334b35690a92f78945";

        String endpoint = "/v2/project/"+apiKey+"/render";
        stubFor(get(urlPathEqualTo(endpoint))
              .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\n" +
                          "  \"count\":2,\n" +
                          "  \"items\":[\n" +
                          "    {\n" +
                          "      \"id\":\"80abaf0d-25a3-4efc-968f-6268d620668d\",\n" +
                          "      \"sessionId\":\"1_MX4yNzA4NjYxMn5-MTU0NzA4MDUyMTEzNn5sOXU5ZnlWYXplRnZGblV4RUo3dXJpZk1-fg\",\n" +
                          "      \"projectId\":\"27086612\",\n" +
                          "      \"createdAt\":1547080511760,\n" +
                          "      \"updatedAt\":1547080518965,\n" +
                          "      \"url\": \"https://webapp2.customer.com\",\n" +
                          "      \"resolution\": \"1280x720\",\n" +
                          "      \"status\":\"started\",\n" +
                          "      \"streamId\": \"d2334b35690a92f78945\",\n" +
                          "      \"reason\":\"Maximum duration exceeded\"\n" +
                          "    },\n" +
                          "    {\n" +
                          "      \"id\":\""+id+"\",\n" +
                          "      \"sessionId\":\""+sessionId+"\",\n" +
                          "      \"projectId\":\""+projectId+"\",\n" +
                          "      \"createdAt\":"+createdAt+",\n" +
                          "      \"updatedAt\":"+updatedAt+",\n" +
                          "      \"url\": \""+url+"\",\n" +
                          "      \"resolution\": \""+resolution+"\",\n" +
                          "      \"status\": \""+status+"\",\n" +
                          "      \"streamId\": \""+streamId+"\"\n" +
                          "    }\n" +
                          "  ]\n" +
                          "}"
                    )
              )
        );

        List<Render> renderList = sdk.listRenders();
        assertNotNull(renderList);
        renderList = sdk.listRenders(1, 5);
        assertEquals(2, renderList.size());
        Render render = renderList.get(1);
        assertEquals(sessionId, render.getSessionId());
        assertEquals(id, render.getId());
        assertEquals(projectId, render.getProjectId());
        assertEquals(createdAt, render.getCreatedAt());
        assertEquals(updatedAt, render.getUpdatedAt());
        assertEquals(url, render.getUrl());
        assertEquals(resolution, render.getResolution());
        assertEquals(status, render.getStatus());
        assertEquals(streamId, render.getStreamId());
        assertNull(render.getReason());

        verify(getRequestedFor(urlMatching(endpoint)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
              findAll(getRequestedFor(urlMatching(endpoint)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testConnectAudioStream200() throws OpenTokException {
        String url = "/v2/project/" + apiKey + "/connect";
        String callId = UUID.randomUUID().toString();
        String connectionId = UUID.randomUUID().toString();
        String sessionId = UUID.randomUUID().toString();
        String uri = "ws://service.com/wsendpoint";

        stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson("{\"sessionId\":\""+sessionId+"\",\"token\":\""+apiSecret+"\",\"websocket\":{\"uri\":\"ws://service.com/wsendpoint\",\"streams\":[\"STREAMID1\",\"STREAMID2\"],\"headers\":{\"key1\":\"header1\",\"content-type\":\"audio/l16;rate=16000\"}}}"))
                .willReturn(aResponse()
                        .withBody("{\"id\": \""+callId+"\", \"connectionId\": \""+connectionId+"\"}")
                        .withStatus(200)
                ));

        AudioConnectorProperties connectProperties = new AudioConnectorProperties.Builder(uri)
                .addHeader("content-type", "audio/l16;rate=16000")
                .addHeader("key1", "header1")
                .addStreams("STREAMID1", "STREAMID2")
                .build();

        AudioConnector connectResponse = sdk.connectAudioStream(sessionId, apiSecret, connectProperties);

        assertNotNull(connectResponse);
        assertEquals(connectionId, connectResponse.getConnectionId());
        assertEquals(callId, connectResponse.getId());
    }

    @Test
    public void testConnectAudioStreamNoHeadersOrStreams() throws OpenTokException {
        String url = "/v2/project/" + apiKey + "/connect";
        String sessionId = UUID.randomUUID().toString();
        String callId = UUID.randomUUID().toString();
        String connectionId = UUID.randomUUID().toString();
        String endpoint = "ws://service.com/wsendpoint";
        String requestJson = "{\"sessionId\":\""+sessionId+"\",\"token\":\""+apiSecret+"\",\"websocket\":{\"uri\":\""+endpoint+"\"}}";

        stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(requestJson))
                .willReturn(aResponse()
                        .withBody("{\"id\": \""+callId+"\", \"connectionId\": \""+connectionId+"\"}")
                        .withStatus(200)
                )
        );

        AudioConnectorProperties connectProperties = new AudioConnectorProperties.Builder(endpoint).build();

        assertTrue(connectProperties.streams() == null || connectProperties.streams().isEmpty());
        assertTrue(connectProperties.headers() == null || connectProperties.headers().isEmpty());

        AudioConnector connectResponse = sdk.connectAudioStream(sessionId, apiSecret, connectProperties);

        assertNotNull(connectResponse);
        assertEquals(connectionId, connectResponse.getConnectionId());
        assertEquals(callId, connectResponse.getId());
    }

    @Test
    public void testConnectAudioStreamUnknownResponseCode() throws OpenTokException {
        String url = "/v2/project/" + apiKey + "/connect";
        String sessionId = UUID.randomUUID().toString();
        String uri = "ws://service.com/wsendpoint";
        String json = "{\"sessionId\":\""+sessionId+"\",\"token\":\""+apiSecret+"\",\"websocket\":{\"uri\":\""+uri+"\"}}";

        stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(json))
                .willReturn(aResponse().withStatus(503))
        );

        AudioConnectorProperties connectProperties = new AudioConnectorProperties.Builder(uri).build();
        assertThrows(RequestException.class, () -> sdk.connectAudioStream(sessionId, apiSecret, connectProperties));
    }

    @Test
    public void testConnectAudioStreamErrors() throws OpenTokException {
        String url = "/v2/project/" + apiKey + "/connect";
        String sessionId = UUID.randomUUID().toString();
        String uri = "ws://service.com/wsendpoint";
        AudioConnectorProperties connectProperties = new AudioConnectorProperties.Builder(uri).build();

        stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(400)));
        assertThrows(RequestException.class, () -> sdk.connectAudioStream(sessionId, apiSecret, connectProperties));

        stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(403)));
        assertThrows(RequestException.class, () -> sdk.connectAudioStream(sessionId, apiSecret, connectProperties));

        stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(404)));
        assertThrows(RequestException.class, () -> sdk.connectAudioStream(sessionId, apiSecret, connectProperties));

        stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(500)));
        assertThrows(RequestException.class, () -> sdk.connectAudioStream(sessionId, apiSecret, connectProperties));
    }

    @Test
    public void testConnectProperties() throws Exception {
        assertThrows(
    "Should not be possible to construct audio stream without URI",
            Exception.class,
            () -> new AudioConnectorProperties.Builder((java.net.URI) null).build()
        );
        assertThrows(
                "Should not be possible to construct audio stream without URI",
                Exception.class,
                () -> new AudioConnectorProperties.Builder((String) null).build()
        );
        String uriStr = "ws://service.com/wsendpoint";
        AudioConnectorProperties cp1 = new AudioConnectorProperties.Builder(new URI(uriStr)).build();
        assertTrue(cp1.headers() == null || cp1.headers().isEmpty());
        assertTrue(cp1.streams() == null || cp1.streams().isEmpty());
        assertNotNull(cp1.uri());
        assertNotNull(cp1.type());

        AudioConnectorProperties cp2 = new AudioConnectorProperties.Builder(uriStr)
            .addStreams(new HashSet<>())
            .addHeaders(new HashMap<>())
            .addStream("STREAMID")
            .addStreams(new LinkedList<>())
            .addHeader("k1", "v1")
            .build();

        assertThrows(UnsupportedOperationException.class, () -> cp2.headers().put("k2", "v2"));
        assertThrows(UnsupportedOperationException.class, () -> cp2.headers().clear());
        assertThrows(UnsupportedOperationException.class, () -> cp2.streams().add("streamID_2"));
        assertThrows(UnsupportedOperationException.class, () -> cp2.streams().clear());

        assertThrows(IllegalArgumentException.class, () ->
                new AudioConnectorProperties.Builder(uriStr).addHeader(" ", "value").build()
        );
        assertThrows(IllegalArgumentException.class, () ->
                new AudioConnectorProperties.Builder(uriStr).addStream(" ").build()
        );
    }

    @Test
    public void testStartCaptions() throws Exception {
        String sessionId = "1_MX4yNzA4NjYxMn5-MTU0NzA4MDUyMTEzNn5sOXU5ZnlWYXplRnZGblV4RUo3dXJpZk1-fg";
        String token = "A valid OpenTok token with the role set to moderator";
        String statusCallbackUrl = "https://send-status-to.me";
        String languageCode = "en-GB";
        int maxDuration = 1800;
        boolean partialCaptions = false;
        String captionsId = "7c0680fc-6274-4de5-a66f-d0648e8d3ac2";
        String url = "/v2/project/" + this.apiKey + "/captions";
        stubFor(post(urlEqualTo(url))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("{\n" +
                        "  \"sessionId\": \""+sessionId+"\",\n" +
                        "  \"token\": \""+token+"\",\n" +
                        "  \"languageCode\": \""+languageCode+"\",\n" +
                        "  \"maxDuration\": "+maxDuration+",\n" +
                        "  \"partialCaptions\": "+partialCaptions+",\n" +
                        "  \"statusCallbackUrl\": \""+statusCallbackUrl+"\"\n" +
                        "}"
                )).willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"captionsId\": \""+captionsId+"\"\n" +
                                "}"
                        )
                )
        );

        CaptionProperties properties = CaptionProperties.Builder()
                .languageCode(languageCode)
                .statusCallbackUrl(statusCallbackUrl)
                .maxDuration(maxDuration)
                .partialCaptions(partialCaptions)
                .build();

        Caption caption = sdk.startCaptions(sessionId, token, properties);
        assertNotNull(caption);
        assertEquals(captionsId, caption.getCaptionsId());
        assertTrue(caption.toString().contains(captionsId));

        verify(postRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
                findAll(postRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();

        assertThrows(InvalidArgumentException.class, () -> sdk.startCaptions("", token, properties));
        assertThrows(InvalidArgumentException.class, () -> sdk.startCaptions(sessionId, "", properties));

        stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(409)));
        assertThrows(RequestException.class, () -> sdk.startCaptions(sessionId, token, null));
    }

    @Test
    public void testCaptionProperties() throws Exception {
        CaptionProperties.Builder builder = CaptionProperties.Builder();
        CaptionProperties properties = builder.build();
        assertEquals(14400, properties.getMaxDuration());
        assertEquals("en-US", properties.getLanguageCode());
        assertTrue(properties.partialCaptions());
        assertNull(properties.getStatusCallbackUrl());

        assertThrows(IllegalArgumentException.class, () -> builder.maxDuration(14401).build());
        assertThrows(IllegalArgumentException.class, () -> builder.maxDuration(-1).build());
        assertThrows(IllegalArgumentException.class, () -> builder.statusCallbackUrl("invalid").build());
        assertThrows(IllegalArgumentException.class, () -> builder.languageCode("invalid").build());
    }

    @Test
    public void testStopCaptions() throws Exception {
        String captionsId = UUID.randomUUID().toString();
        String url = "/v2/project/" + this.apiKey + "/captions/" + captionsId + "/stop";
        stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(202)));

        sdk.stopCaptions(captionsId);

        verify(postRequestedFor(urlMatching(url)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
                findAll(deleteRequestedFor(urlMatching(url)))));
        Helpers.verifyUserAgent();

        assertThrows(InvalidArgumentException.class, () -> sdk.stopCaptions(""));
        stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(404)));
        assertThrows(RequestException.class, () -> sdk.stopCaptions(captionsId));
    }
}