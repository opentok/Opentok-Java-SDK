/**
 * OpenTok Java SDK
 * Copyright (C) 2017 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ning.http.client.*;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.FilterException;
import com.ning.http.client.filter.RequestFilter;
import com.opentok.ArchiveProperties;
import com.opentok.constants.DefaultApiUrl;
import com.opentok.constants.Version;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.RequestException;

public class HttpClient extends AsyncHttpClient {
    
    private final String apiUrl;
    private final int apiKey;

    private HttpClient(Builder builder) {
        super(builder.config);
        this.apiKey = builder.apiKey;
        this.apiUrl = builder.apiUrl;
    }

    public String createSession(Map<String, Collection<String>> params) throws RequestException {
        String responseString = null;
        Response response = null;
        FluentStringsMap paramsString = new FluentStringsMap().addAll(params);

        Future<Response> request = this.preparePost(this.apiUrl + "/session/create")
                .setFormParams(paramsString)
                .addHeader("Accept", "application/json") // XML version is deprecated
                .execute();

        try {
            response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    responseString = response.getResponseBody();
                    break;
                default:
                    throw new RequestException("Could not create an OpenTok Session. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RequestException("Could not create an OpenTok Session", e);
        }
        return responseString;
    }

    public String getArchive(String archiveId) throws RequestException {
        String responseString = null;
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId;
        Future<Response> request = this.prepareGet(url).execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    responseString = response.getResponseBody();
                    break;
                case 400:
                    throw new RequestException("Could not get an OpenTok Archive. The archiveId was invalid. " +
                            "archiveId: " + archiveId);
                case 403:
                    throw new RequestException("Could not get an OpenTok Archive. The request was not authorized.");
                case 500:
                    throw new RequestException("Could not get an OpenTok Archive. A server error occurred.");
                default:
                    throw new RequestException("Could not get an OpenTok Archive. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RequestException("Could not  get an OpenTok Archive", e);
        }

        return responseString;
    }

    public String getArchives(int offset, int count) throws RequestException {
        String responseString = null;
        // TODO: maybe use a StringBuilder?
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive";
        if (offset != 0 || count != 1000) {
            url += "?";
            if (offset != 0) {
                url += ("offset=" + Integer.toString(offset) + '&');
            }
            if (count != 1000) {
                url += ("count=" + Integer.toString(count));
            }
        }

        Future<Response> request = this.prepareGet(url).execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    responseString = response.getResponseBody();
                    break;
                case 403:
                    throw new RequestException("Could not get OpenTok Archives. The request was not authorized.");
                case 500:
                    throw new RequestException("Could not get OpenTok Archives. A server error occurred.");
                default:
                    throw new RequestException("Could not get an OpenTok Archive. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RequestException("Could not get OpenTok Archives", e);
        }

        return responseString;
    }

    public String getArchives(String sessionId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive?sessionId=" + sessionId;

        Future<Response> request = this.prepareGet(url).execute();
        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 403:
                    throw new RequestException("Could not get OpenTok Archives. The request was not authorized.");
                case 500:
                    throw new RequestException("Could not get OpenTok Archives. A server error occurred.");
                default:
                    throw new RequestException("Could not get an OpenTok Archive. The server response was invalid."
                            + " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RequestException("Could not get OpenTok Archives", e);
        }
    }

    public String startArchive(String sessionId, ArchiveProperties properties)
            throws OpenTokException {
        String responseString = null;
        String requestBody = null;
        // TODO: maybe use a StringBuilder?
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive";

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();
        requestJson.put("sessionId", sessionId);
        requestJson.put("hasVideo", properties.hasVideo());
        requestJson.put("hasAudio", properties.hasAudio());
        requestJson.put("outputMode", properties.outputMode().toString());

        if (properties.name() != null) {
            requestJson.put("name", properties.name());
        }
        try {
            requestBody = new ObjectMapper().writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not start an OpenTok Archive. The JSON body encoding failed.", e);
        }
        Future<Response> request = this.preparePost(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    responseString = response.getResponseBody();
                    break;
                case 403:
                    throw new RequestException("Could not start an OpenTok Archive. The request was not authorized.");
                case 404:
                    throw new RequestException("Could not start an OpenTok Archive. The sessionId does not exist. " +
                            "sessionId = " + sessionId);
                case 409:
                    throw new RequestException("Could not start an OpenTok Archive. The session is either " +
                            "peer-to-peer or already recording. sessionId = " + sessionId);
                case 500:
                    throw new RequestException("Could not start an OpenTok Archive. A server error occurred.");
                default:
                    throw new RequestException("Could not start an OpenTok Archive. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RequestException("Could not start an OpenTok Archive.", e);
        }
        return responseString;
    }

    public String stopArchive(String archiveId) throws RequestException {
        String responseString = null;
        // TODO: maybe use a StringBuilder?
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId + "/stop";
        Future<Response> request = this.preparePost(url).execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    responseString = response.getResponseBody();
                    break;
                case 400:
                    // NOTE: the REST api spec talks about sessionId and action, both of which aren't required.
                    //       see: https://github.com/opentok/OpenTok-2.0-archiving-samples/blob/master/REST-API.md#stop_archive
                    throw new RequestException("Could not stop an OpenTok Archive.");
                case 403:
                    throw new RequestException("Could not stop an OpenTok Archive. The request was not authorized.");
                case 404:
                    throw new RequestException("Could not stop an OpenTok Archive. The archiveId does not exist. " +
                            "archiveId = " + archiveId);
                case 409:
                    throw new RequestException("Could not stop an OpenTok Archive. The archive is not being recorded. " +
                            "archiveId = " + archiveId);
                case 500:
                    throw new RequestException("Could not stop an OpenTok Archive. A server error occurred.");
                default:
                    throw new RequestException("Could not stop an OpenTok Archive. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RequestException("Could not stop an OpenTok Archive.", e);
        }
        return responseString;
    }

    public String deleteArchive(String archiveId) throws RequestException {
        String responseString = null;
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId;
        Future<Response> request = this.prepareDelete(url).execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 204:
                    responseString = response.getResponseBody();
                    break;
                case 403:
                    throw new RequestException("Could not delete an OpenTok Archive. The request was not authorized.");
                case 409:
                    throw new RequestException("Could not delete an OpenTok Archive. The status was not \"uploaded\"," +
                            " \"available\", or \"deleted\". archiveId = " + archiveId);
                case 500:
                    throw new RequestException("Could not delete an OpenTok Archive. A server error occurred.");
                default:
                    throw new RequestException("Could not get an OpenTok Archive. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RequestException("Could not delete an OpenTok Archive. archiveId = " + archiveId, e);
        }

        return responseString;
    }

    public static class Builder {
        private final int apiKey;
        private final String apiSecret;
        private Proxy proxy;
        private String apiUrl;
        private AsyncHttpClientConfig config;

        public Builder(int apiKey, String apiSecret) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
        }

        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public HttpClient build() {
            AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder()
                    .setUserAgent("Opentok-Java-SDK/" + Version.VERSION + " JRE/" + System.getProperty("java.version"))
                    .addRequestFilter(new TokenAuthRequestFilter(this.apiKey, this.apiSecret));
            if (this.apiUrl == null) {
                this.apiUrl=DefaultApiUrl.DEFAULT_API_URI;
            }
            
            if (this.proxy != null) {
                configBuilder.setProxyServer(createProxyServer(this.proxy));
            }
            
            this.config = configBuilder.build();
            // NOTE: not thread-safe, config could be modified by another thread here?
            HttpClient client = new HttpClient(this);
            return client;
        }

        // credit: https://github.com/AsyncHttpClient/async-http-client/blob/b52a8de5d6a862b5d1652d62f87ce774cbcff156/src/main/java/com/ning/http/client/ProxyServer.java#L99-L127
        static ProxyServer createProxyServer(final Proxy proxy) {
            switch (proxy.type()) {
                case DIRECT:
                    return null;
                case SOCKS:
                    throw new IllegalArgumentException("Only DIRECT and HTTP Proxies are supported!");
            }

            final SocketAddress sa = proxy.address();

            if (!(sa instanceof InetSocketAddress)) {
                throw new IllegalArgumentException("Only Internet Address sockets are supported!");
            }

            InetSocketAddress isa = (InetSocketAddress) sa;

            final String isaHost = isa.isUnresolved() ? isa.getHostName() : isa.getAddress().getHostAddress();
            return new ProxyServer(isaHost, isa.getPort());
        }
    }

    static class TokenAuthRequestFilter implements RequestFilter {

        private final int apiKey;
        private final String apiSecret;
        private final String authHeader = "X-OPENTOK-AUTH";

        public TokenAuthRequestFilter(int apiKey, String apiSecret) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
        }

        public FilterContext filter(FilterContext ctx) throws FilterException {
            try {
                return new FilterContext.FilterContextBuilder(ctx)
                        .request(new RequestBuilder(ctx.getRequest())
                                .addHeader(authHeader, TokenGenerator.generateToken(apiKey, apiSecret))
                                .build())
                        .build();
            } catch (OpenTokException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
