/**
 * OpenTok Java SDK
 * Copyright (C) 2017 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.opentok.*;
import com.opentok.Archive.OutputMode;
import com.opentok.exception.InvalidArgumentException;
import com.opentok.exception.OpenTokException;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class OpenTokTest {

    private final String SESSION_CREATE = "/session/create";
    private int apiKey = 123456;
    private String archivePath = "/v2/project/" + apiKey + "/archive";
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
        assertEquals(apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());
        assertEquals(MediaMode.RELAYED, session.getProperties().mediaMode());
        assertEquals(locationHint, session.getProperties().getLocation());

        verify(postRequestedFor(urlMatching(SESSION_CREATE))
                // TODO: this is a pretty bad way to verify, ideally we can decode the body and then query the object
                .withRequestBody(matching(".*location="+locationHint+".*")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
                findAll(postRequestedFor(urlMatching(SESSION_CREATE)))));
        Helpers.verifyUserAgent();
    }

    @Test
    public void testCreateAlwaysArchivedSession() throws OpenTokException {
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
                .archiveMode(ArchiveMode.ALWAYS)
                .build();
        Session session = sdk.createSession(properties);

        assertNotNull(session);
        assertEquals(apiKey, session.getApiKey());
        assertEquals(sessionId, session.getSessionId());
        assertEquals(ArchiveMode.ALWAYS, session.getProperties().archiveMode());


        verify(postRequestedFor(urlMatching(SESSION_CREATE))
                // TODO: this is a pretty bad way to verify, ideally we can decode the body and then query the object
                .withRequestBody(matching(".*archiveMode=always.*")));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
                findAll(postRequestedFor(urlMatching(SESSION_CREATE)))));
        Helpers.verifyUserAgent();
    }

    @Test(expected = InvalidArgumentException.class)
    public void testCreateBadSession() throws OpenTokException {
        SessionProperties properties = new SessionProperties.Builder()
                .location("NOT A VALID IP")
                .build();
    }

//    This is not part of the API because it would introduce a backwards incompatible change.
//    @Test(expected = InvalidArgumentException.class)
//    public void testCreateInvalidAlwaysArchivedAndRelayedSession() throws OpenTokException {
//        SessionProperties properties = new SessionProperties.Builder()
//                .mediaMode(MediaMode.RELAYED)
//                .archiveMode(ArchiveMode.ALWAYS)
//                .build();
//    }

    // TODO: test session creation conditions that result in errors

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

        int apiKey = 123456;
        String apiSecret = "1234567890abcdef1234567890abcdef1234567890";
        OpenTok opentok = new OpenTok(apiKey, apiSecret);
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
    public void testTokenRoles() throws
            OpenTokException, UnsupportedEncodingException, NoSuchAlgorithmException,
            SignatureException, InvalidKeyException {

        int apiKey = 123456;
        String apiSecret = "1234567890abcdef1234567890abcdef1234567890";
        OpenTok opentok = new OpenTok(apiKey, apiSecret);
        String sessionId = "1_MX4xMjM0NTZ-flNhdCBNYXIgMTUgMTQ6NDI6MjMgUERUIDIwMTR-MC40OTAxMzAyNX4";
        Role role = Role.SUBSCRIBER;

        String defaultToken = opentok.generateToken(sessionId);
        String roleToken = sdk.generateToken(sessionId, new TokenOptions.Builder()
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
        long inOneHour = now + (60 * 60);
        long inOneDay = now + (60 * 60 * 24);
        long inThirtyDays = now + (60 * 60 * 24 * 30);
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
        assertEquals(apiKey, archive.getPartnerId());
        assertEquals(archiveId, archive.getId());
        assertEquals(1395187836000L, archive.getCreatedAt());
        assertEquals(62, archive.getDuration());
        assertEquals("", archive.getName());
        assertEquals("SESSIONID", archive.getSessionId());
        assertEquals(8347554, archive.getSize());
        assertEquals(Archive.Status.AVAILABLE, archive.getStatus());
        assertEquals("http://tokbox.com.archive2.s3.amazonaws.com/123456%2F"+archiveId +"%2Farchive.mp4?Expires=13951" +
                "94362&AWSAccessKeyId=AKIAI6LQCPIXYVWCQV6Q&Signature=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", archive.getUrl());

        verify(getRequestedFor(urlMatching(archivePath + "/" + archiveId)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
                findAll(getRequestedFor(urlMatching(archivePath + "/" + archiveId)))));
        Helpers.verifyUserAgent();
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

        ArchiveList archives = sdk.listArchives();

        assertNotNull(archives);
        assertEquals(6, archives.size());
        assertEquals(60, archives.getTotalCount());
        assertThat(archives.get(0), instanceOf(Archive.class));
        assertEquals("ef546c5a-4fd7-4e59-ab3d-f1cfb4148d1d", archives.get(0).getId());

        verify(getRequestedFor(urlMatching(archivePath)));
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
                findAll(getRequestedFor(urlMatching(archivePath)))));
        Helpers.verifyUserAgent();
    }

    // TODO: test list archives with count and offset

    // TODO: test list archives failure scenarios

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

        ArchiveProperties properties = new ArchiveProperties.Builder().name(null).build();
        Archive archive = sdk.startArchive(sessionId, properties);
        assertNotNull(archive);
        assertEquals(sessionId, archive.getSessionId());
        assertNotNull(archive.getId());

        verify(postRequestedFor(urlMatching(archivePath)));
                // TODO: find a way to match JSON without caring about spacing
                //.withRequestBody(matching(".*"+".*"))
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
                findAll(postRequestedFor(urlMatching(archivePath)))));
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
                // TODO: find a way to match JSON without caring about spacing
                //.withRequestBody(matching(".*"+".*"))
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
                // TODO: find a way to match JSON without caring about spacing
                //.withRequestBody(matching(".*"+".*"))
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
                // TODO: find a way to match JSON without caring about spacing
                //.withRequestBody(matching(".*"+".*"))
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
                // TODO: find a way to match JSON without caring about spacing
                //.withRequestBody(matching(".*"+".*"))
        assertTrue(Helpers.verifyTokenAuth(apiKey, apiSecret,
                findAll(postRequestedFor(urlMatching(archivePath)))));
        Helpers.verifyUserAgent();
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
                // TODO: find a way to match JSON without caring about spacing
                //.withRequestBody(matching(".*"+".*"))
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
    @Test public void testGetExpiredArchive() throws OpenTokException {
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
    @Test public void testGetPausedArchive() throws OpenTokException {
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

    @Test public void testGetArchiveWithUnknownProperties() throws OpenTokException {
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
        sdk = new OpenTok.Builder(apiKey, apiSecret).apiUrl(targetServiceBaseUrl).proxy(proxy).build();
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
}
