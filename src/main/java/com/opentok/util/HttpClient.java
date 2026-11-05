/**
 * OpenTok Java SDK
 * Copyright (C) 2026 Vonage.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opentok.*;
import com.opentok.constants.DefaultApiUrl;
import com.opentok.constants.DefaultUserAgent;
import com.opentok.exception.InvalidArgumentException;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.RequestException;
import com.vonage.jwt.Jwt;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class HttpClient implements Closeable {
    // Defaults carried over from the previous async-http-client configuration.
    static final int DEFAULT_REQUEST_TIMEOUT_MS = 60_000;
    static final int DEFAULT_CONNECT_TIMEOUT_MS = 5_000;
    static final int MAX_CONNECTIONS = 100;

    private final String apiUrl, apiKey;
    private final CloseableHttpClient httpClient;

    private HttpClient(Builder builder) {
        apiKey = builder.apiKey;
        apiUrl = builder.apiUrl;
        httpClient = builder.httpClient;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Releases pooled connections held by the underlying HTTP client.
     */
    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            // Nothing actionable for the caller if releasing connections fails.
        }
    }

    private Request prepareGet(String url) {
        return new Request("GET", url);
    }

    private Request preparePost(String url) {
        return new Request("POST", url);
    }

    private Request preparePut(String url) {
        return new Request("PUT", url);
    }

    private Request preparePatch(String url) {
        return new Request("PATCH", url);
    }

    private Request prepareDelete(String url) {
        return new Request("DELETE", url);
    }

    public String createSession(Map<String, List<String>> params) throws RequestException {
        Request request = this.preparePost(this.apiUrl + "/session/create")
                .setFormParams(params)
                .setHeader("Accept", "application/json"); // XML version is deprecated

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                default:
                    throw new RequestException("Could not create an OpenTok Session. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not create an OpenTok Session", e);
        }
    }

    public String signal(String sessionId, String connectionId, SignalProperties properties) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + (connectionId != null && !connectionId.isEmpty() ? "/connection/" + connectionId : "") + "/signal";
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();

        requestJson.put("type", properties.type());
        requestJson.put("data", properties.data());

        String requestBody;
        try {
            requestBody = new ObjectMapper().writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not send a signal. The JSON body encoding failed.", e);
        }
        Request request = this.preparePost(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 204:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not send a signal: "+response.getResponseBody());
                case 403:
                    throw new RequestException("Could not send a signal. The request was not authorized.");
                case 404:
                    throw new RequestException("Could not send a signal. The client specified by the connectionId property is not connected to the session.");
                case 413:
                    throw new RequestException("Could not send a signal. The type string exceeds the maximum length (128 bytes), or the data string exceeds the maximum size (8 kB)");
                default:
                    throw new RequestException("Could not send a signal " +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not send a signal.", e);
        }
    }

    public String getArchive(String archiveId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId;
        Request request = this.prepareGet(url)
                .setHeader("Accept", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
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
        } catch (IOException e) {
            throw new RequestException("Could not  get an OpenTok Archive", e);
        }
    }

    public String getArchives(String sessionId, int offset, int count) throws OpenTokException {
        if (offset < 0 || count < 0 || count > 1000) {
            throw new InvalidArgumentException("Make sure count parameter value is >= 0 and/or offset parameter value is <=1000");
        }

        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive";
        if (offset != 0 || count != 1000) {
            url += "?";
            if (offset != 0) {
                url += ("offset=" + offset + '&');
            }
            if (count != 1000) {
                url += ("count=" + count);
            }
        }
        if (sessionId != null && !sessionId.isEmpty()) {
            url += (url.contains("?") ? "&" : "?") + "sessionId=" + sessionId;
        }

        Request request = this.prepareGet(url)
                .setHeader("Accept", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 403:
                    throw new RequestException("Could not get OpenTok Archives. The request was not authorized.");
                case 500:
                    throw new RequestException("Could not get OpenTok Archives. A server error occurred.");
                default:
                    throw new RequestException("Could not get OpenTok Archives. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not get OpenTok Archives", e);
        }
    }

    public String startArchive(String sessionId, ArchiveProperties properties) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive";

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode()
            .put("sessionId", sessionId)
            .put("hasVideo", properties.hasVideo())
            .put("hasAudio", properties.hasAudio())
            .put("outputMode", properties.outputMode().toString())
            .put("streamMode", properties.streamMode().toString());

        if (properties.layout() != null) {
            ObjectNode layout = requestJson.putObject("layout");
            layout.put("type", properties.layout().getType().toString());
            if (properties.layout().getScreenshareType() != null) {
                if (properties.layout().getType() != ArchiveLayout.Type.BESTFIT) {
                    throw new InvalidArgumentException("Could not start Archive. When screenshareType is set in the layout, type must be bestFit");
                }
                layout.put("screenshareType", properties.layout().getScreenshareType().toString());
            }
            if (!(properties.layout().getStylesheet() == null)) {
                layout.put("stylesheet", properties.layout().getStylesheet());
            }
        }
        if (properties.name() != null) {
            requestJson.put("name", properties.name());
        }
        if (properties.resolution() != null) {
            requestJson.put("resolution", properties.resolution());
        }
        if (properties.maxBitrate() != null) {
            requestJson.put("maxBitrate", properties.maxBitrate());
        }
        if (properties.getMultiArchiveTag() != null) {
            requestJson.put("multiArchiveTag", properties.getMultiArchiveTag());
        }

        String requestBody;
        try {
            requestBody = new ObjectMapper().writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not start an OpenTok Archive. The JSON body encoding failed.", e);
        }
        Request request = this.preparePost(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not start an OpenTok Archive: "+response.getResponseBody());
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
        } catch (IOException e) {
            throw new RequestException("Could not start an OpenTok Archive.", e);
        }
    }

    public String stopArchive(String archiveId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId + "/stop";
        Request request = this.preparePost(url)
                .setHeader("Accept", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not stop an OpenTok Archive: "+response.getResponseBody());
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
        } catch (IOException e) {
            throw new RequestException("Could not stop an OpenTok Archive.", e);
        }
    }

    public String deleteArchive(String archiveId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId;
        Request request = this.prepareDelete(url);

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 204:
                    return response.getResponseBody();
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
        } catch (IOException e) {
            throw new RequestException("Could not delete an OpenTok Archive. archiveId = " + archiveId, e);
        }
    }

    public String patchArchive(String archiveId, String addStream, String removeStream, boolean hasAudio, boolean hasVideo) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId + "/streams";

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();
        if (removeStream != null && !removeStream.isEmpty()) {
            requestJson.put("removeStream", removeStream);
        }
        else if (addStream != null && !addStream.isEmpty()) {
            requestJson.put("hasAudio", hasAudio);
            requestJson.put("hasVideo", hasVideo);
            requestJson.put("addStream", addStream);
        }
        else {
            throw new InvalidArgumentException("Could not patch archive, needs one of: addStream or removeStream");
        }

        String requestBody;
        try {
            requestBody = new ObjectMapper().writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not patch OpenTok archive. The JSON body encoding failed");
        }

        Request request = this.preparePatch(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 204:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not patch OpenTok archive: "+response.getResponseBody());
                case 404:
                    throw new RequestException("Could not patch OpenTok archive. Archive or stream not found.");
                case 405:
                    throw new RequestException("Could not patch OpenTok archive. Stream mode not supported for patching.");
                case 403:
                    throw new RequestException("Could not patch OpenTok archive. The request was unauthorized.");
                case 500:
                    throw new RequestException("Could not patch OpenTok archive. A server error occurred");
                default:
                    throw new RequestException("Could not patch OpenTok archive. The server response was invalid. Response code: " +
                            response.getStatusCode());
            }

        } catch (IOException e) {
            throw new RequestException("Could not patch an OpenTok archive.", e);
        }
    }

    public String setArchiveLayout(String archiveId, ArchiveProperties properties) throws OpenTokException {
        if (properties.layout() == null) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        String type = properties.layout().getType().toString();
        String stylesheet = properties.layout().getStylesheet();
        String screenshareType = null;

        if (StringUtils.isEmpty(type)) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        if ((type.equals(ArchiveLayout.Type.CUSTOM.toString()) && StringUtils.isEmpty(stylesheet)) ||
                (!type.equals(ArchiveLayout.Type.CUSTOM.toString()) && !StringUtils.isEmpty(stylesheet))) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        if (properties.layout().getScreenshareType() != null) {
            if (properties.layout().getType() != ArchiveLayout.Type.BESTFIT) {
                throw new InvalidArgumentException("Could not set the Archive layout. When screenshareType is set, type must be bestFit");
            }
            screenshareType = properties.layout().getScreenshareType().toString();
        }

        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId + "/layout";
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();
        requestJson.put("type", type);

        if (type.equals(ArchiveLayout.Type.CUSTOM.toString())) {
            requestJson.put("stylesheet", properties.layout().getStylesheet());
        }
        if (screenshareType != null) {
            requestJson.put("screenshareType", screenshareType);
        }

        String requestBody;
        try {
            requestBody = new ObjectMapper().writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not set the layout. The JSON body encoding failed.", e);
        }
        Request request = this.preparePut(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not set the layout: "+response.getResponseBody());
                case 403:
                    throw new RequestException("Could not set the layout. The request was not authorized.");
                case 500:
                    throw new RequestException("Could not set the layout. A server error occurred.");
                default:
                    throw new RequestException("Could not set the layout. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not set the layout, archiveId = " + archiveId, e);
        }
    }

    public String setStreamLayouts(String sessionId, StreamListProperties properties) throws OpenTokException {
        char doubleQuotes = '"';
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/stream";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JsonFactory factory = new JsonFactory();
            //Using JsonGenerator as layoutClassList values must be in double quotes and ObjectMapper
            // adds extra escape characters
            JsonGenerator jGenerator = factory.createGenerator(outputStream);
            jGenerator.writeStartObject();
            jGenerator.writeArrayFieldStart("items");

            for (StreamProperties stream : properties.getStreamList()) {
                jGenerator.writeStartObject();
                jGenerator.writeFieldName("id");
                jGenerator.writeString(stream.id());
                jGenerator.writeFieldName("layoutClassList");
                List<String> stringList = stream.getLayoutClassList();
                StringJoiner sj = new StringJoiner(",");
                stringList.forEach(e -> sj.add(doubleQuotes + e + doubleQuotes));
                jGenerator.writeRawValue("[" + sj + "]");
                jGenerator.writeEndObject();
            }

            jGenerator.writeEndArray();
            jGenerator.writeEndObject();
            jGenerator.close();
            outputStream.close();
        } catch (Exception e) {
            throw new OpenTokException("Could not set the layout. The JSON body encoding failed.", e);
        }

        Request request = this.preparePut(url)
                .setBody(outputStream.toString())
                .setHeader("Content-Type", "application/json");
        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not set the layout: "+response.getResponseBody());
                case 403:
                    throw new RequestException("Could not set the layout. The request was not authorized.");
                case 500:
                    throw new RequestException("Could not set the layout. A server error occurred.");
                default:
                    throw new RequestException("Could not set the layout. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not delete an OpenTok Archive, sessionId = " + sessionId, e);
        }
    }

    public String startBroadcast(String sessionId, BroadcastProperties properties) throws OpenTokException {
        ScreenShareLayoutType screenshareType;

        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/broadcast";

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();
        requestJson.put("sessionId", sessionId);
        requestJson.put("streamMode", properties.streamMode().toString());
        requestJson.put("hasAudio", properties.hasAudio());
        requestJson.put("hasVideo", properties.hasVideo());

        if (properties.layout() != null) {
            ObjectNode layout = requestJson.putObject("layout");
            screenshareType = properties.layout().getScreenshareType();
            String type = properties.layout().getType().toString();
            layout.put("type", type);
            if (screenshareType != null && !type.equals(ArchiveLayout.Type.BESTFIT.toString())) {
                throw new InvalidArgumentException("Could not start OpenTok Broadcast, Layout Type must be bestfit when screenshareType is set.");
            }
            if (screenshareType != null) {
                layout.put("screenshareType", screenshareType.toString());
            }
            if (type.equals(BroadcastLayout.Type.CUSTOM.toString())) {
                layout.put("stylesheet", properties.layout().getStylesheet());
            }
        }
        if (properties.maxDuration() > 0) {
            requestJson.put("maxDuration", properties.maxDuration());
        }
        if (properties.maxBitrate() > 0) {
            requestJson.put("maxBitrate", properties.maxBitrate());
        }
        if (properties.resolution() != null) {
            requestJson.put("resolution", properties.resolution());
        }
        if (properties.getMultiBroadcastTag() != null) {
            requestJson.put("multiBroadcastTag", properties.getMultiBroadcastTag());
        }

        ObjectNode outputs = requestJson.putObject("outputs");
        if (properties.hasHls()) {
            ObjectNode hlsNode = nodeFactory.objectNode();
            outputs.set("hls", hlsNode);
            Hls hlsPojo = properties.hls();
            if (hlsPojo != null) {
                hlsNode.put("dvr", hlsPojo.dvr());
                hlsNode.put("lowLatency", hlsPojo.lowLatency());
            }
        }

        ArrayNode rtmp = outputs.putArray("rtmp");
        for (RtmpProperties prop : properties.rtmpList()) {
            ObjectNode rtmpProps = nodeFactory.objectNode();
            rtmpProps.put("id", prop.id());
            rtmpProps.put("serverUrl", prop.serverUrl());
            rtmpProps.put("streamName", prop.streamName());
            rtmp.add(rtmpProps);
        }

        String requestBody;
        try {
            requestBody = new ObjectMapper().writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not start an OpenTok Broadcast. The JSON body encoding failed.", e);
        }

        Request request = this.preparePost(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not start an OpenTok Broadcast: "+response.getResponseBody());
                case 403:
                    throw new RequestException("Could not start an OpenTok Broadcast. The request was not authorized.");
                case 409:
                    throw new RequestException("The broadcast has already been started for the session. SessionId = " + sessionId);
                case 500:
                    throw new RequestException("Could not start an OpenTok Broadcast. A server error occurred.");
                default:
                    throw new RequestException("Could not start an OpenTok Broadcast. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not start an OpenTok Broadcast.", e);
        }
    }

    public String stopBroadcast(String broadcastId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId + "/stop";

        Request request = this.preparePost(url)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not start an OpenTok Broadcast: "+response.getResponseBody());
                case 403:
                    throw new RequestException("Could not start an OpenTok Broadcast. The request was not authorized.");
                case 404:
                    throw new RequestException("The broadcast " + broadcastId + "was not found or it has already stopped.");
                case 500:
                    throw new RequestException("Could not start an OpenTok Broadcast. A server error occurred.");
                default:
                    throw new RequestException("Could not start an OpenTok Broadcast. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not start an OpenTok Broadcast.", e);
        }
    }

    public String getBroadcast(String broadcastId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId;

        Request request = this.prepareGet(url)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not get Broadcast stream information: "+response.getResponseBody());
                case 403:
                    throw new RequestException("Could not get Broadcast stream information.. The request was not authorized.");
                case 404:
                    throw new RequestException("The broadcast " + broadcastId + "was not found.");
                case 500:
                    throw new RequestException("Could not get Broadcast stream information.. A server error occurred.");
                default:
                    throw new RequestException("Could not get Broadcast stream information.The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not get Broadcast stream information.", e);
        }
    }

    public String patchBroadcast(String broadcastId, String addStream, String removeStream, boolean hasAudio, boolean hasVideo) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId + "/streams";
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();

        if (removeStream != null && !removeStream.isEmpty()) {
            requestJson.put("removeStream", removeStream);
        }
        else if(addStream != null && !addStream.isEmpty()) {
            requestJson.put("hasAudio", hasAudio);
            requestJson.put("hasVideo", hasVideo);
            requestJson.put("addStream", addStream);
        }
        else {
            throw new InvalidArgumentException("Could not patch broadcast, needs one of: addStream or removeStream");
        }

        String requestBody;
        try {
            requestBody = new ObjectMapper().writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not patch OpenTok archive. The JSON body encoding failed");
        }

        Request request = this.preparePatch(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json");
        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 204:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not patch OpenTok broadcast: "+response.getResponseBody());
                case 404:
                    throw new RequestException("Could not patch OpenTok broadcast. Archive or stream not found.");
                case 405:
                    throw new RequestException("Could not patch OpenTok broadcast. Stream mode not supported for patching.");
                case 403:
                    throw new RequestException("Could not patch OpenTok broadcast. The request was unauthorized.");
                case 500:
                    throw new RequestException("Could not patch OpenTok broadcast. A server error occurred");
                default:
                    throw new RequestException("Could not patch OpenTok broadcast. The server response was invalid. Response code: " +
                            response.getStatusCode());
            }

        } catch (IOException e) {
            throw new RequestException("Could not patch an OpenTok broadcast.", e);
        }
    }

    public String setBroadcastLayout(String broadcastId, BroadcastProperties properties) throws OpenTokException {
        if (properties.layout() == null) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        String type = properties.layout().getType().toString();
        String stylesheet = properties.layout().getStylesheet();
        String screenshareLayout = null;

        if (StringUtils.isEmpty(type)) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        if ((type.equals(BroadcastLayout.Type.CUSTOM.toString()) && StringUtils.isEmpty(stylesheet)) ||
                (!type.equals(BroadcastLayout.Type.CUSTOM.toString()) && !StringUtils.isEmpty(stylesheet))) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        if (properties.layout().getScreenshareType() != null) {
            if (properties.layout().getType() != ArchiveLayout.Type.BESTFIT) {
                throw new InvalidArgumentException("Could not set layout. Type must be bestfit when screenshareLayout is set.");
            }
            screenshareLayout = properties.layout().getScreenshareType().toString();
        }

        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId + "/layout";

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();
        requestJson.put("type", type);

        if (type.equals(BroadcastLayout.Type.CUSTOM.toString())) {
            requestJson.put("stylesheet", properties.layout().getStylesheet());
        }
        if (screenshareLayout != null) {
            requestJson.put("screenshareType", screenshareLayout);
        }

        String requestBody;
        try {
            requestBody = new ObjectMapper().writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not set the layout. The JSON body encoding failed.", e);
        }
        Request request = this.preparePut(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not set the layout: "+response.getResponseBody());
                case 403:
                    throw new RequestException("Could not set the layout. The request was not authorized.");
                case 500:
                    throw new RequestException("Could not set the layout. A server error occurred.");
                default:
                    throw new RequestException("Could not set the layout. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not set the layout, broadcastId = " + broadcastId, e);
        }
    }

    public String forceDisconnect(String sessionId, String connectionId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/connection/" + connectionId;
        Request request = this.prepareDelete(url);

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 204:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not force disconnect: "+response.getResponseBody());
                case 403:
                    throw new RequestException("Could not force disconnect. You are not authorized to forceDisconnect, check your authentication credentials.");
                case 404:
                    throw new RequestException("Could not force disconnect. The client specified by the connectionId property is not connected to the session.");
                default:
                    throw new RequestException("Could not force disconnect. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not force disconnect", e);
        }
    }

    public String sipDial(String sessionId, String token, SipProperties props) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/dial";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Character dQuotes = '"';
        try {
            JsonFactory factory = new JsonFactory();
            JsonGenerator jGenerator = factory.createGenerator(outputStream);

            jGenerator.writeStartObject();       //main object
            jGenerator.writeFieldName("sessionId");
            jGenerator.writeString(sessionId);
            jGenerator.writeFieldName("token");
            jGenerator.writeString(token);
            jGenerator.writeFieldName("sip");
            jGenerator.writeStartObject();       //start sip
            jGenerator.writeFieldName("uri");
            jGenerator.writeRawValue(dQuotes + props.sipUri() + dQuotes);
            if (!StringUtils.isEmpty(props.from())) {
                jGenerator.writeFieldName("from");
                jGenerator.writeRawValue(dQuotes + props.from() + dQuotes);
            }
            if (!StringUtils.isEmpty(props.headersJsonStartingWithXDash())) {
                jGenerator.writeFieldName("headers");
                jGenerator.writeRawValue(props.headersJsonStartingWithXDash());
            }
            if (!StringUtils.isEmpty(props.userName()) && !StringUtils.isEmpty(props.password())) {
                jGenerator.writeFieldName("auth");
                jGenerator.writeStartObject();
                jGenerator.writeFieldName("username");
                jGenerator.writeRawValue(dQuotes + props.userName() + dQuotes);
                jGenerator.writeFieldName("password");
                jGenerator.writeRawValue(dQuotes + props.password() + dQuotes);
                jGenerator.writeEndObject();
            }

            jGenerator.writeFieldName("secure");
            jGenerator.writeBoolean(props.secure());

            jGenerator.writeFieldName("video");
            jGenerator.writeBoolean(props.video());

            jGenerator.writeFieldName("observeForceMute");
            jGenerator.writeBoolean(props.observeForceMute());

            String[] streams = props.streams();
            if (streams != null && streams.length > 0) {
                jGenerator.writeArrayFieldStart("streams");
                for (String streamId : streams) {
                    jGenerator.writeString(streamId);
                }
                jGenerator.writeEndArray();
            }

            jGenerator.writeEndObject();      // end sip
            jGenerator.writeEndObject();      // end main object
            jGenerator.close();
            outputStream.close();

        } catch (Exception e) {
            throw new OpenTokException("Could not set the sip dial. The JSON body encoding failed.", e);
        }

        Request request = this.preparePost(url)
                .setBody(outputStream.toString())
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Could not set the SIP dial. Either an invalid sessionId or the custom header does not start with the X- prefix.");
                case 403:
                    throw new RequestException("Could not set the sip dial. The request was not authorized.");
                case 404:
                    throw new RequestException("Could not set the sip dial. The session does not exist.");
                case 409:
                    throw new RequestException("Could not set the sip dial.  A SIP call should use the OpenTok routed mode.");
                case 500:
                    throw new RequestException("Could not set the sip dial. A server error occurred.");
                default:
                    throw new RequestException("Could not set the sip dial. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not set the sip dial, sessionId = " + sessionId, e);
        }
    }

    public String playDtmf(String url, String dtmfDigits) throws OpenTokException {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();
        requestJson.put("digits", dtmfDigits);

        String requestBody;
        try {
            requestBody = new ObjectMapper().writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not send a signal. The JSON body encoding failed.", e);
        }
        Request request = this.preparePost(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                default:
                    throw new RequestException("Could not get a proper response. response code: " +
                            response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not play dtmf");
        }
    }

    public String playDtmfAll(String sessionId, String dtmfDigits) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/play-dtmf";
        return playDtmf(url, dtmfDigits);
    }

    public String playDtmfSingle(String sessionId, String connectionId, String dtmfDigits) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId +
                "/connection/"+ connectionId + "/play-dtmf";
        return playDtmf(url, dtmfDigits);
    }

    public String getStream(String sessionId, String streamId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/stream/" + streamId;
        Request request = this.prepareGet(url);

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException(response.getResponseBody());
                case 403:
                    throw new RequestException("Invalid OpenTok API key or JWT token.");
                case 408:
                    throw new RequestException("You passed in an invalid stream ID: " + streamId);
                case 500:
                    throw new RequestException("OpenTok server error.");
                default:
                    throw new RequestException("Could not get stream information. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not get stream information", e);
        }
    }

    public String forceMuteStream(String sessionId, String streamId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/stream/" + streamId + "/mute";
        Request request = this.preparePost(url);

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException(response.getResponseBody());
                case 403:
                    throw new RequestException("Invalid OpenTok API key or JWT token.");
                case 408:
                    throw new RequestException("You passed in an invalid stream ID: "+streamId);
                case 500:
                    throw new RequestException("OpenTok server error.");
                default:
                    throw new RequestException("Could not mute stream. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not get stream information", e);
        }
    }

    public String forceMuteAllStream(String sessionId, MuteAllProperties properties) throws OpenTokException {
        char doubleQuotes = '"';
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/mute";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JsonFactory factory = new JsonFactory();
            //Using JsonGenerator as layoutClassList values must be in double quotes and ObjectMapper
            // adds extra escape characters
            JsonGenerator jGenerator = factory.createGenerator(outputStream);
            jGenerator.writeStartObject();
            jGenerator.writeBooleanField("active", true);

            jGenerator.writeFieldName("excludedStreamIds");

            StringJoiner sj = new StringJoiner(",");
            properties.getExcludedStreamIds().forEach(e -> sj.add(doubleQuotes + e + doubleQuotes));
            jGenerator.writeRawValue("[" + sj + "]");
            jGenerator.writeEndObject();

            jGenerator.close();
            outputStream.close();
        } catch (Exception e) {
            throw new OpenTokException("Could not force mute streams The JSON body encoding failed.", e);
        }

        Request request = this.preparePost(url)
                .setBody(outputStream.toString())
                .setHeader("Content-Type", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException(response.getResponseBody());
                case 403:
                    throw new RequestException("Invalid OpenTok API key or JWT token.");
                case 408:
                    throw new RequestException("You passed in an invalid stream ID.");
                case 500:
                    throw new RequestException("OpenTok server error.");
                default:
                    throw new RequestException("Could not mute stream. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not get stream information", e);
        }
    }

    public String disableForceMute(String sessionId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/mute";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JsonFactory factory = new JsonFactory();
            //Using JsonGenerator as layoutClassList values must be in double quotes and ObjectMapper
            // adds extra escape characters
            JsonGenerator jGenerator = factory.createGenerator(outputStream);
            jGenerator.writeStartObject();
            jGenerator.writeBooleanField("active", false);
            jGenerator.writeEndObject();

            jGenerator.close();
            outputStream.close();
        } catch (Exception e) {
            throw new OpenTokException("Could not force mute streams. The JSON body encoding failed.", e);
        }

        Request request = this.preparePost(url)
                .setBody(outputStream.toString())
                .setHeader("Content-Type", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException(response.getResponseBody());
                case 403:
                    throw new RequestException("Invalid OpenTok API key or JWT token.");
                case 408:
                    throw new RequestException("You passed in an invalid stream ID.");
                case 500:
                    throw new RequestException("OpenTok server error.");
                default:
                    throw new RequestException("Could not mute stream. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not get stream information", e);
        }
    }

    public String listStreams(String sessionId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/stream";
        Request request = this.prepareGet(url)
                .setHeader("Accept", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException(response.getResponseBody());
                case 403:
                    throw new RequestException("You passed in an invalid OpenTok API key or JWT token");
                case 408:
                    throw new RequestException("Could not get information for streams. The session Id may be invalid.");
                case 500:
                    throw new RequestException("Could not get information for streams. A server error occurred.");
                default:
                    throw new RequestException("Could not get information for streams. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not get streams information", e);
        }
    }

    public String listConnections(String sessionId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/connection";
        Request request = this.prepareGet(url)
                .setHeader("Accept", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException(response.getResponseBody());
                case 403:
                    throw new RequestException("You passed in an invalid OpenTok API key or JWT token.");
                case 404:
                    throw new RequestException("Session not found: " + sessionId);
                case 500:
                    throw new RequestException("Could not list connections. A server error occurred.");
                default:
                    throw new RequestException("Could not list connections. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not list connections", e);
        }
    }

    public String connectAudioStream(String sessionId, String token, AudioConnectorProperties properties) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/connect";

        ObjectNode requestJson = JsonNodeFactory.instance.objectNode()
                .put("sessionId", sessionId)
                .put("token", token);

        ObjectNode mainBody = requestJson.putObject(properties.type());
        mainBody.put("uri", properties.uri().toString());
        Collection<String> streamsProperty = properties.streams();
        if (streamsProperty != null && !streamsProperty.isEmpty()) {
            ArrayNode streams = mainBody.putArray("streams");
            streamsProperty.forEach(streams::add);
        }
        Map<String, String> headersProperty = properties.headers();
        if (headersProperty != null && !headersProperty.isEmpty()) {
            ObjectNode headers = mainBody.putObject("headers");
            headersProperty.forEach(headers::put);
        }

        String requestBody;
        try {
            requestBody = new ObjectMapper().writeValueAsString(requestJson);
        } catch (JsonProcessingException ex) {
            throw new OpenTokException("Could not connect audio stream(s). The JSON body encoding failed", ex);
        }

        Request request = preparePost(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException(response.getResponseBody());
                case 403:
                    throw new RequestException("Invalid OpenTok API key or JWT token.");
                case 409:
                    throw new RequestException("Conflict. Only routed sessions are allowed to initiate Connect Calls.");
                case 500:
                    throw new RequestException("OpenTok server error.");
                default:
                    throw new RequestException("Could not connect audio stream. The server response was invalid." +
                            " Response code: " + response.getStatusCode());
            }
        } catch (IOException ex) {
            throw new RequestException("Could not get stream information", ex);
        }
    }

    public String startRender(String sessionId, String token, RenderProperties properties) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/render";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JsonFactory factory = new JsonFactory();
            JsonGenerator jGenerator = factory.createGenerator(outputStream);
            jGenerator.writeStartObject();
            jGenerator.writeStringField("sessionId", sessionId);
            jGenerator.writeStringField("token", token);
            jGenerator.writeStringField("url", properties.url().toString());
            jGenerator.writeNumberField("maxDuration", properties.maxDuration());
            if (properties.resolution() != null) {
                jGenerator.writeStringField("resolution", properties.resolution().toString());
            }
            if (properties.properties() != null) {
                jGenerator.writeObjectFieldStart("properties");
                jGenerator.writeStringField("name", properties.properties().name());
                jGenerator.writeEndObject();
            }
            jGenerator.writeEndObject();
            jGenerator.close();
            outputStream.close();
        }
        catch (Exception e) {
            throw new OpenTokException("Could not start render. The JSON body encoding failed.", e);
        }

        Request request = this.preparePost(url)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .setBody(outputStream.toString());

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 202:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Invalid request. This response may indicate that data in your request data is invalid JSON. Or it may indicate that you do not pass in a session ID");
                case 403:
                    throw new RequestException("You passed in an invalid OpenTok API key or JWT token.");
                case 500:
                    throw new RequestException("Could not start render. A server error occurred.");
                default:
                    throw new RequestException("Could not start render. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not start render", e);
        }
    }

    public String getRender(String renderId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/render/" + renderId;

        Request request = this.prepareGet(url)
                .setHeader("Accept", "application/json");

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Invalid request. This response may indicate that data in your request data is invalid JSON.");
                case 403:
                    throw new RequestException("You passed in an invalid OpenTok API key or JWT token.");
                case 404:
                    throw new RequestException("No Render matching the specified ID was found.");
                case 500:
                    throw new RequestException("Could not get render. A server error occurred.");
                default:
                    throw new RequestException("Could not get render. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not get render '"+renderId+"'", e);
        }
    }

    public void stopRender(String renderId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/render/" + renderId;
        try {
            Response response = this.prepareDelete(url).execute();
            switch (response.getStatusCode()) {
                case 200:
                    return;
                case 400:
                    throw new RequestException("Invalid request. This response may indicate that data in your request data is invalid JSON.");
                case 403:
                    throw new RequestException("You passed in an invalid OpenTok API key or JWT token.");
                case 404:
                    throw new RequestException("No Render matching the specified ID was found.");
                case 500:
                    throw new RequestException("Could not stop render. A server error occurred.");
                default:
                    throw new RequestException("Could not stop render. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not stop render", e);
        }
    }

    public String listRenders(Integer offset, Integer count) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/render";

        Request rqBuilder = this.prepareGet(url).setHeader("Accept", "application/json");
        if (offset != null) {
            rqBuilder.addQueryParam("offset", offset.toString());
        }
        if (count != null) {
            rqBuilder.addQueryParam("count", count.toString());
        }

        try {
            Response response = rqBuilder.execute();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 403:
                    throw new RequestException("You passed in an invalid OpenTok API key or JWT token");
                case 500:
                    throw new RequestException("Could not list renders. A server error occurred.");
                default:
                    throw new RequestException("Could not list renders. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not start render", e);
        }
    }

    public String startCaption(String sessionId, String token, CaptionProperties properties) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/captions";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JsonFactory factory = new JsonFactory();
            JsonGenerator jGenerator = factory.createGenerator(outputStream);
            jGenerator.writeStartObject();
            jGenerator.writeStringField("sessionId", sessionId);
            jGenerator.writeStringField("token", token);
            jGenerator.writeStringField("languageCode", properties.getLanguageCode());
            jGenerator.writeNumberField("maxDuration", properties.getMaxDuration());
            jGenerator.writeBooleanField("partialCaptions", properties.partialCaptions());
            String statusCallbackUrl = properties.getStatusCallbackUrl();
            if (StringUtils.isNotEmpty(statusCallbackUrl)) {
                jGenerator.writeStringField("statusCallbackUrl", statusCallbackUrl);
            }
            jGenerator.writeEndObject();
            jGenerator.close();
            outputStream.close();
        }
        catch (Exception e) {
            throw new OpenTokException("Could not start live captions. The JSON body encoding failed.", e);
        }

        Request request = this.preparePost(url)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .setBody(outputStream.toString());

        try {
            Response response = request.execute();
            switch (response.getStatusCode()) {
                case 200: case 202:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException("Invalid request. This response may indicate that data in your request data is invalid JSON.");
                case 403:
                    throw new RequestException("You passed in an invalid OpenTok API key or JWT.");
                case 409:
                    throw new RequestException("Live captions have already started for this OpenTok session.");
                case 500:
                    throw new RequestException("Could not stop live captions. A server error occurred.");
                default:
                    throw new RequestException("Could not stop render. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not stop captions", e);
        }
    }

    public void stopCaption(String captionsId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/captions/" + captionsId + "/stop";
        try {
            Response response = this.preparePost(url).execute();
            switch (response.getStatusCode()) {
                case 200: case 202:
                    return;
                case 403:
                    throw new RequestException("You passed in an invalid OpenTok API key or JWT.");
                case 404:
                    throw new RequestException("No live caption matching the specified ID was found.");
                case 500:
                    throw new RequestException("Could not stop live captions. A server error occurred.");
                default:
                    throw new RequestException("Could not stop render. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RequestException("Could not stop captions", e);
        }
    }

    public enum ProxyAuthScheme {
        BASIC,
        DIGEST,
        NTLM,
        SPNEGO,
        KERBEROS
    }

    public static class Builder {
        private final String apiKey;
        private final String apiSecret;
        private final Path privateKeyPath;
        private final boolean vonage;
        private Proxy proxy;
        private ProxyAuthScheme proxyAuthScheme;
        private String principal;
        private String password;
        private String apiUrl;
        private String userAgent = DefaultUserAgent.DEFAULT_USER_AGENT;
        private CloseableHttpClient httpClient;
        private int requestTimeoutMS;

        public Builder(int apiKey, String apiSecret) {
            this(apiKey, apiSecret, null, null);
        }

        public Builder(String applicationId, Path privateKeyPath) {
            this(UUID.fromString(applicationId), privateKeyPath);
        }

        public Builder(UUID applicationId, Path privateKeyPath) {
            this(0, null, applicationId.toString(), privateKeyPath);
        }

        public Builder(int apiKey, String apiSecret, String applicationId, Path privateKeyPath) {
            vonage = applicationId != null;
            this.apiKey = vonage ? applicationId : apiKey + "";
            this.apiSecret = apiSecret;
            this.privateKeyPath = privateKeyPath;
        }

        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        public Builder proxy(Proxy proxy) {
            proxy(proxy, null, null, null);
            return this;
        }

        public Builder proxy(Proxy proxy, ProxyAuthScheme proxyAuthScheme, String principal, String password) {
            this.proxy = proxy;
            this.proxyAuthScheme = proxyAuthScheme;
            this.principal = principal;
            this.password = password;
            return this;
        }

        /**
         * Specify a custom timeout value for HTTP requests when initalizing a new {@link OpenTok} object.
         *
         * @param requestTimeoutMS request timeout in milliseconds
         * @return Builder
         */
        public Builder requestTimeoutMS(int requestTimeoutMS) {
            this.requestTimeoutMS = requestTimeoutMS;
            return this;
        }

        /**
         * Sets the user agent to a custom value.
         *
         * @param userAgent The user agent.
         *
         * @return This Builder with user agent string.
         */
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public HttpClient build() {
            if (apiUrl == null) {
                apiUrl = vonage ? DefaultApiUrl.VONAGE_API_URL : DefaultApiUrl.DEFAULT_API_URI;
            }

            RequestConfig.Builder requestConfig = createRequestConfig(requestTimeoutMS);
            HttpClientBuilder clientBuilder = HttpClientBuilder.create()
                    .setUserAgent(userAgent)
                    // async-http-client did not follow redirects; keep that behavior.
                    .disableRedirectHandling()
                    .setMaxConnTotal(MAX_CONNECTIONS)
                    .setMaxConnPerRoute(MAX_CONNECTIONS)
                    .addInterceptorLast(vonage ?
                            new TokenAuthInterceptor(UUID.fromString(apiKey), privateKeyPath) :
                            new TokenAuthInterceptor(Integer.parseInt(apiKey), apiSecret)
                    );

            if (proxy != null) {
                HttpHost proxyHost = createProxyHost(proxy);
                if (proxyHost != null) {
                    clientBuilder.setProxy(proxyHost);
                    if (principal != null) {
                        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(
                                new AuthScope(proxyHost),
                                createProxyCredentials(proxyAuthScheme, principal, password)
                        );
                        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                        if (proxyAuthScheme != null) {
                            requestConfig.setProxyPreferredAuthSchemes(
                                    Collections.singletonList(toAuthSchemeName(proxyAuthScheme))
                            );
                        }
                    }
                }
            }

            httpClient = clientBuilder.setDefaultRequestConfig(requestConfig.build()).build();
            return new HttpClient(this);
        }

        /**
         * Maps the single request timeout onto Apache HttpClient's timeouts. A value of 0 uses the
         * 60 second default; a negative value disables the timeout. The connect timeout keeps the
         * previous 5 second default unless the request timeout is shorter.
         */
        static RequestConfig.Builder createRequestConfig(int requestTimeoutMS) {
            int timeout = requestTimeoutMS == 0 ? DEFAULT_REQUEST_TIMEOUT_MS : Math.max(requestTimeoutMS, 0);
            int connectTimeout = timeout == 0 ? DEFAULT_CONNECT_TIMEOUT_MS : Math.min(DEFAULT_CONNECT_TIMEOUT_MS, timeout);
            return RequestConfig.custom()
                    .setConnectTimeout(connectTimeout)
                    .setConnectionRequestTimeout(timeout)
                    .setSocketTimeout(timeout);
        }

        static HttpHost createProxyHost(final Proxy proxy) {
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
            return new HttpHost(isaHost, isa.getPort());
        }

        static Credentials createProxyCredentials(ProxyAuthScheme proxyAuthScheme, String principal, String password) {
            if (proxyAuthScheme == ProxyAuthScheme.NTLM) {
                // Apache's NTLM scheme only accepts NTCredentials.
                return new NTCredentials(principal, password, null, null);
            }
            // SPNEGO and KERBEROS take their credentials from the JAAS/GSS configuration;
            // a credentials entry is still required for the scheme to be attempted.
            return new UsernamePasswordCredentials(principal, password);
        }

        static String toAuthSchemeName(ProxyAuthScheme proxyAuthScheme) {
            switch (proxyAuthScheme) {
                case BASIC:
                    return AuthSchemes.BASIC;
                case DIGEST:
                    return AuthSchemes.DIGEST;
                case NTLM:
                    return AuthSchemes.NTLM;
                case SPNEGO:
                    return AuthSchemes.SPNEGO;
                case KERBEROS:
                    return AuthSchemes.KERBEROS;
                default:
                    throw new IllegalArgumentException("Unsupported proxy auth scheme: " + proxyAuthScheme);
            }
        }
    }

    static class TokenAuthInterceptor implements HttpRequestInterceptor {
        private final Supplier<String> tokenGenerator;
        private final String headerName;

        public TokenAuthInterceptor(int apiKey, String apiSecret) {
            headerName = "X-OPENTOK-AUTH";
            tokenGenerator = () -> TokenGenerator.generateToken(apiKey, apiSecret);
        }

        public TokenAuthInterceptor(UUID applicationId, Path privateKeyPath) {
            headerName = "Authorization";
            try {
                Jwt jwtGenerator = Jwt.builder().applicationId(applicationId).privateKeyPath(privateKeyPath).build();
                tokenGenerator = () -> "Bearer " + jwtGenerator.generate();
            }
            catch (IOException ex) {
                throw new InvalidArgumentException("Could not create a JWT generator: " + ex);
            }
        }

        /**
         * Sets a freshly generated token on every request, replacing any previous value
         * (for example when a request is retried).
         */
        @Override
        public void process(HttpRequest request, HttpContext context) {
            request.setHeader(headerName, tokenGenerator.get());
        }
    }

    /**
     * Minimal request builder used by the API methods above.
     */
    private final class Request {
        private final String method;
        private final String url;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private final List<NameValuePair> queryParams = new ArrayList<>();
        private HttpEntity entity;

        private Request(String method, String url) {
            this.method = method;
            this.url = url;
        }

        Request setHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        Request setBody(String body) {
            entity = new ByteArrayEntity(body.getBytes(StandardCharsets.UTF_8));
            return this;
        }

        Request setFormParams(Map<String, List<String>> params) {
            List<NameValuePair> pairs = new ArrayList<>();
            params.forEach((name, values) -> values.forEach(value -> pairs.add(new BasicNameValuePair(name, value))));
            entity = new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8);
            return this;
        }

        Request addQueryParam(String name, String value) {
            queryParams.add(new BasicNameValuePair(name, value));
            return this;
        }

        Response execute() throws IOException {
            RequestBuilder builder;
            try {
                URI uri = new URI(url);
                if (!queryParams.isEmpty()) {
                    uri = new URIBuilder(uri).addParameters(queryParams).build();
                }
                builder = RequestBuilder.create(method).setUri(uri);
            } catch (URISyntaxException e) {
                throw new ClientProtocolException("Invalid request URL: " + url, e);
            }
            headers.forEach(builder::setHeader);
            if (entity != null) {
                builder.setEntity(entity);
            } else if (!"GET".equals(method) && !"DELETE".equals(method)) {
                // Send an explicit "Content-Length: 0" on body-less POST/PUT/PATCH requests.
                builder.setEntity(new ByteArrayEntity(new byte[0]));
            }

            try (CloseableHttpResponse response = httpClient.execute(builder.build())) {
                HttpEntity responseEntity = response.getEntity();
                String body = responseEntity != null ? EntityUtils.toString(responseEntity, StandardCharsets.UTF_8) : "";
                return new Response(response.getStatusLine().getStatusCode(), body);
            }
        }
    }

    private static final class Response {
        private final int statusCode;
        private final String responseBody;

        private Response(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        int getStatusCode() {
            return statusCode;
        }

        String getResponseBody() {
            return responseBody;
        }
    }
}
