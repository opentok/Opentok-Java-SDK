/**
 * OpenTok Java SDK
 * Copyright (C) 2016 TokBox, Inc.
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
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.FluentStringsMap;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.FilterException;
import com.ning.http.client.filter.RequestFilter;
import com.opentok.ArchiveProperties;
import com.opentok.CallbackEvent;
import com.opentok.CallbackGroup;
import com.opentok.Signal;
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
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive/" + archiveId;
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
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive";
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
                    throw new RequestException("Could not get OpenTok Archives. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RequestException("Could not get OpenTok Archives", e);
        }

        return responseString;
    }

    public String startArchive(String sessionId, ArchiveProperties properties)
            throws OpenTokException {
        String responseString = null;
        String requestBody = null;
        // TODO: maybe use a StringBuilder?
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive";

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
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive/" + archiveId + "/stop";
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
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive/" + archiveId;
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

    public String registerCallback(CallbackGroup group, CallbackEvent event, String callbackUrl) throws OpenTokException {
        String responseString = null;
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/callback";

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();
        requestJson.put("group", group.toString());
        requestJson.put("event", event.toString());
        requestJson.put("url", callbackUrl);

        String requestBody;
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
            if (response.getStatusCode() / 100 == 2) {
                responseString = response.getResponseBody();
            } else {
                switch (response.getStatusCode()) {
                    case 400:
                        throw new RequestException("Could not register an OpenTok Callback.");
                    case 403:
                        throw new RequestException("Could not register an OpenTok Callback. The request was not authorized.");
                    case 500:
                        throw new RequestException("Could not register an OpenTok Callback. A server error occurred.");
                    default:
                        throw new RequestException("Could not register an OpenTok Callback. The server response was invalid." +
                                " response code: " + response.getStatusCode());
                }
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RequestException("Could not register an OpenTok Callback.", e);
        }
        return responseString;
    }

    public void unregisterCallback(String callbackId) throws OpenTokException {
        String responseString = null;
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/callback/" + callbackId;


        Future<Response> request = this.prepareDelete(url).execute();

        try {
            Response response = request.get();
            if (response.getStatusCode() / 100 != 2) {
                switch (response.getStatusCode()) {
                    case 400:
                        throw new RequestException("Could not unregister an OpenTok Callback.");
                    case 404:
                        throw new RequestException("Could not unregister an OpenTok Callback. The callback was not found.");
                    case 403:
                        throw new RequestException("Could not unregister an OpenTok Callback. The request was not authorized.");
                    case 500:
                        throw new RequestException("Could not unregister an OpenTok Callback. A server error occurred.");
                    default:
                        throw new RequestException("Could not unregister an OpenTok Callback. The server response was invalid." +
                                " response code: " + response.getStatusCode());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not unregister an OpenTok Callback.", e);
        }
    }

    public String getCallbacks() throws RequestException {
        String responseString = null;
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/callback";

        Future<Response> request = this.prepareGet(url).execute();

        try {
            Response response = request.get();
            if (response.getStatusCode() / 100 == 2) {
                responseString = response.getResponseBody();
            } else {
                switch (response.getStatusCode()) {
                    case 403:
                        throw new RequestException("Could not get OpenTok Callbacks. The request was not authorized.");
                    case 500:
                        throw new RequestException("Could not get OpenTok Callbacks. A server error occurred.");
                    default:
                        throw new RequestException("Could not get OpenTok Callbacks. The server response was invalid." +
                                " response code: " + response.getStatusCode());
                }
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RequestException("Could not get OpenTok Archives", e);
        }

        return responseString;
    }

    public void signal(String sessionId, String connectionId, Signal payload) throws OpenTokException {
        String responseString = null;
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/session/" + sessionId;
        if (connectionId != null) {
            url += "/connection/" + connectionId;
        }
        url += "/signal";

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();
        if (payload.getType() != null) {
            requestJson.put("type", payload.getType());
        }
        if (payload.getData() != null) {
            requestJson.put("data", payload.getData());
        }

        String requestBody;
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
            if (response.getStatusCode() / 100 != 2) {
                switch (response.getStatusCode()) {
                    case 400:
                        throw new RequestException("Could not send a signal.");
                    case 404:
                        throw new RequestException("Could not send a signal. The connection or session was not found.");
                    case 403:
                        throw new RequestException("Could not send a signal. The request was not authorized.");
                    case 500:
                        throw new RequestException("Could not send a signal. A server error occurred.");
                    default:
                        throw new RequestException("Could not send a signal. The server response was invalid." +
                                " response code: " + response.getStatusCode());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not register an OpenTok Callback.", e);
        }
    }


    public void forceDisconnect(String sessionId, String connectionId) throws OpenTokException {
        String responseString = null;
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/session/" + sessionId + "/connection/" + connectionId;

        Future<Response> request = this.prepareDelete(url)
                .execute();

        try {
            Response response = request.get();
            if (response.getStatusCode() / 100 != 2) {
                switch (response.getStatusCode()) {
                    case 400:
                        throw new RequestException("Could not force a disconnect.");
                    case 404:
                        throw new RequestException("Could not force a disconnect. The connection or session was not found.");
                    case 403:
                        throw new RequestException("Could not force a disconnect. The request was not authorized.");
                    case 500:
                        throw new RequestException("Could not force a disconnect. A server error occurred.");
                    default:
                        throw new RequestException("Could not force a disconnect. The server response was invalid." +
                                " response code: " + response.getStatusCode());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not register an OpenTok Callback.", e);
        }
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
            if (proxy.type().equals(Proxy.Type.DIRECT)) {
                return null;
            }

            if (!proxy.type().equals(Proxy.Type.HTTP)) {
                throw new IllegalArgumentException("Only DIRECT and HTTP Proxies are supported!");
            }

            final SocketAddress sa = proxy.address();

            if (!(sa instanceof InetSocketAddress)) {
                throw new IllegalArgumentException("Only Internet Address sockets are supported!");
            }

            InetSocketAddress isa = (InetSocketAddress) sa;

            if (isa.isUnresolved()) {
                return new ProxyServer(isa.getHostName(), isa.getPort());
            } else {
                return new ProxyServer(isa.getAddress().getHostAddress(), isa.getPort());
            }
        }
    }

    static class TokenAuthRequestFilter implements RequestFilter {

        private int apiKey;
        private String apiSecret;
        private final String authHeader = "X-OPENTOK-AUTH";

        public TokenAuthRequestFilter(int apiKey, String apiSecret) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
        }

        public FilterContext filter(FilterContext ctx) throws FilterException {
            try {
                return new FilterContext.FilterContextBuilder(ctx)
                        .request(new RequestBuilder(ctx.getRequest())
                                .addHeader(authHeader,
                                        TokenGenerator.generateToken(apiKey, apiSecret))
                                .build())
                        .build();
            } catch (OpenTokException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
