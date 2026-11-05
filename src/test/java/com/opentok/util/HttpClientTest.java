/**
 * OpenTok Java SDK
 * Copyright (C) 2026 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.opentok.TestHelpers;
import com.opentok.exception.RequestException;
import com.opentok.util.HttpClient.ProxyAuthScheme;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.*;

public class HttpClientTest {
    private static final int API_KEY = 123456;
    private static final String API_SECRET = "1234567890abcdef1234567890abcdef1234567890";
    private static final String SESSION_ID = "SESSION_ID";
    private static final String STREAM_ID = "STREAM_ID";
    private static final String STREAM_PATH = "/v2/project/" + API_KEY + "/session/" + SESSION_ID + "/stream/" + STREAM_ID;

    @Rule
    public WireMockRule target = new WireMockRule(wireMockConfig().dynamicPort());

    private WireMockServer proxyServer;
    private HttpClient client;

    @After
    public void tearDown() {
        if (client != null) {
            client.close();
        }
        if (proxyServer != null) {
            proxyServer.stop();
        }
    }

    private String targetUrl() {
        return "http://localhost:" + target.port();
    }

    private HttpClient.Builder legacyBuilder() {
        return new HttpClient.Builder(API_KEY, API_SECRET).apiUrl(targetUrl());
    }

    private static Path testResource(String name) throws Exception {
        return Paths.get(HttpClientTest.class.getResource("/com/opentok/" + name).toURI());
    }

    @Test
    public void legacyCredentialsSendFreshOpenTokAuthHeaderOnEachRequest() throws Exception {
        target.stubFor(get(urlEqualTo(STREAM_PATH)).willReturn(aResponse().withStatus(200).withBody("{}")));
        client = legacyBuilder().build();

        assertEquals("{}", client.getStream(SESSION_ID, STREAM_ID));
        assertEquals("{}", client.getStream(SESSION_ID, STREAM_ID));

        List<LoggedRequest> requests = target.findAll(getRequestedFor(urlEqualTo(STREAM_PATH)));
        assertEquals(2, requests.size());
        SecretKeySpec key = new SecretKeySpec(API_SECRET.getBytes(), "HmacSHA256");
        String firstJti = null;
        for (LoggedRequest request : requests) {
            assertFalse(request.containsHeader("Authorization"));
            assertEquals(1, request.getHeaders().getHeader("X-OPENTOK-AUTH").values().size());
            String token = request.getHeader("X-OPENTOK-AUTH");
            assertTrue(TestHelpers.verifyJWTClaims(token, API_KEY, API_SECRET));
            String jti = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getId();
            if (firstJti == null) {
                firstJti = jti;
            } else {
                assertNotEquals("Each request must carry a newly generated token", firstJti, jti);
            }
        }
    }

    @Test
    public void vonageCredentialsSendBearerJwt() throws Exception {
        UUID applicationId = UUID.randomUUID();
        String path = "/v2/project/" + applicationId + "/session/" + SESSION_ID + "/stream";
        target.stubFor(get(urlEqualTo(path)).willReturn(aResponse().withStatus(200).withBody("{}")));
        client = new HttpClient.Builder(applicationId, testResource("application_key")).apiUrl(targetUrl()).build();

        assertEquals("{}", client.listStreams(SESSION_ID));

        LoggedRequest request = target.findAll(getRequestedFor(urlEqualTo(path))).get(0);
        assertFalse(request.containsHeader("X-OPENTOK-AUTH"));
        String authorization = request.getHeader("Authorization");
        assertTrue(authorization.startsWith("Bearer "));

        X509EncodedKeySpec spec = new X509EncodedKeySpec(Files.readAllBytes(testResource("application_public_key.der")));
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
        Claims claims = Jwts.parser().verifyWith(publicKey).build()
                .parseSignedClaims(authorization.substring("Bearer ".length())).getPayload();
        assertEquals(applicationId.toString(), claims.get("application_id"));
    }

    @Test
    public void requestTimeoutAbortsSlowResponse() {
        target.stubFor(get(urlEqualTo(STREAM_PATH))
                .willReturn(aResponse().withStatus(200).withFixedDelay(1500).withBody("{}")));
        client = legacyBuilder().requestTimeoutMS(500).build();

        RequestException ex = assertThrows(RequestException.class, () -> client.getStream(SESSION_ID, STREAM_ID));
        assertTrue(ex.getCause() instanceof SocketTimeoutException);
    }

    @Test
    public void bodylessPostSendsZeroContentLength() throws Exception {
        String path = STREAM_PATH + "/mute";
        target.stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(200).withBody("{}")));
        client = legacyBuilder().build();

        client.forceMuteStream(SESSION_ID, STREAM_ID);

        target.verify(postRequestedFor(urlEqualTo(path)).withHeader("Content-Length", equalTo("0")));
    }

    @Test
    public void redirectsAreNotFollowed() {
        target.stubFor(get(urlEqualTo(STREAM_PATH))
                .willReturn(aResponse().withStatus(302).withHeader("Location", targetUrl() + "/elsewhere")));
        client = legacyBuilder().build();

        RequestException ex = assertThrows(RequestException.class, () -> client.getStream(SESSION_ID, STREAM_ID));
        assertTrue(ex.getMessage().contains("302"));
        target.verify(0, getRequestedFor(urlEqualTo("/elsewhere")));
    }

    @Test
    public void requestsAreRoutedThroughHttpProxy() throws Exception {
        proxyServer = new WireMockServer(wireMockConfig().dynamicPort());
        proxyServer.start();
        proxyServer.stubFor(any(anyUrl()).willReturn(aResponse().proxiedFrom(targetUrl())));
        target.stubFor(get(urlEqualTo(STREAM_PATH)).willReturn(aResponse().withStatus(200).withBody("{}")));

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", proxyServer.port()));
        client = legacyBuilder().proxy(proxy).build();

        assertEquals("{}", client.getStream(SESSION_ID, STREAM_ID));

        proxyServer.verify(getRequestedFor(urlEqualTo(STREAM_PATH)));
        target.verify(getRequestedFor(urlEqualTo(STREAM_PATH)).withHeader("X-OPENTOK-AUTH", matching(".+")));
    }

    @Test
    public void proxyBasicAuthAnswersChallenge() throws Exception {
        String expected = "Basic " + Base64.getEncoder()
                .encodeToString("proxyUser:proxyPass".getBytes(StandardCharsets.UTF_8));
        proxyServer = new WireMockServer(wireMockConfig().dynamicPort());
        proxyServer.start();
        proxyServer.stubFor(any(anyUrl()).atPriority(1)
                .withHeader("Proxy-Authorization", equalTo(expected))
                .willReturn(aResponse().withStatus(200).withBody("{\"proxied\":true}")));
        proxyServer.stubFor(any(anyUrl()).atPriority(10)
                .willReturn(aResponse().withStatus(407).withHeader("Proxy-Authenticate", "Basic realm=\"test\"")));

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", proxyServer.port()));
        client = legacyBuilder().proxy(proxy, ProxyAuthScheme.BASIC, "proxyUser", "proxyPass").build();

        assertEquals("{\"proxied\":true}", client.getStream(SESSION_ID, STREAM_ID));
        proxyServer.verify(getRequestedFor(urlEqualTo(STREAM_PATH)).withHeader("Proxy-Authorization", equalTo(expected)));
    }

    @Test
    public void proxyAuthSchemesMapToApacheSchemes() {
        assertEquals(AuthSchemes.BASIC, HttpClient.Builder.toAuthSchemeName(ProxyAuthScheme.BASIC));
        assertEquals(AuthSchemes.DIGEST, HttpClient.Builder.toAuthSchemeName(ProxyAuthScheme.DIGEST));
        assertEquals(AuthSchemes.NTLM, HttpClient.Builder.toAuthSchemeName(ProxyAuthScheme.NTLM));
        assertEquals(AuthSchemes.SPNEGO, HttpClient.Builder.toAuthSchemeName(ProxyAuthScheme.SPNEGO));
        assertEquals(AuthSchemes.KERBEROS, HttpClient.Builder.toAuthSchemeName(ProxyAuthScheme.KERBEROS));

        assertTrue(HttpClient.Builder.createProxyCredentials(ProxyAuthScheme.NTLM, "user", "pass") instanceof NTCredentials);
        assertTrue(HttpClient.Builder.createProxyCredentials(ProxyAuthScheme.DIGEST, "user", "pass") instanceof UsernamePasswordCredentials);
    }

    @Test
    public void proxyTypesAreValidated() {
        assertNull(HttpClient.Builder.createProxyHost(Proxy.NO_PROXY));
        Proxy socks = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 1080));
        assertThrows(IllegalArgumentException.class, () -> HttpClient.Builder.createProxyHost(socks));
    }

    @Test
    public void requestTimeoutMapsOntoApacheTimeouts() {
        RequestConfig defaults = HttpClient.Builder.createRequestConfig(0).build();
        assertEquals(HttpClient.DEFAULT_REQUEST_TIMEOUT_MS, defaults.getSocketTimeout());
        assertEquals(HttpClient.DEFAULT_REQUEST_TIMEOUT_MS, defaults.getConnectionRequestTimeout());
        assertEquals(HttpClient.DEFAULT_CONNECT_TIMEOUT_MS, defaults.getConnectTimeout());

        RequestConfig shortTimeout = HttpClient.Builder.createRequestConfig(3000).build();
        assertEquals(3000, shortTimeout.getSocketTimeout());
        assertEquals(3000, shortTimeout.getConnectTimeout());

        RequestConfig noTimeout = HttpClient.Builder.createRequestConfig(-1).build();
        assertEquals(0, noTimeout.getSocketTimeout());
        assertEquals(HttpClient.DEFAULT_CONNECT_TIMEOUT_MS, noTimeout.getConnectTimeout());
    }
}
