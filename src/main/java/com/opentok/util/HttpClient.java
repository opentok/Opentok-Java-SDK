/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
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
import org.apache.commons.lang.StringUtils;
import org.asynchttpclient.*;
import org.asynchttpclient.Realm.AuthScheme;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.FilterException;
import org.asynchttpclient.filter.RequestFilter;
import org.asynchttpclient.proxy.ProxyServer;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HttpClient extends DefaultAsyncHttpClient {
    private final String apiUrl;
    private final int apiKey;

    private HttpClient(Builder builder) {
        super(builder.config);
        this.apiKey = builder.apiKey;
        this.apiUrl = builder.apiUrl;
    }

    public String createSession(Map<String, List<String>> params) throws RequestException {
        Future<Response> request = this.preparePost(this.apiUrl + "/session/create")
                .setFormParams(params)
                .setHeader("Accept", "application/json") // XML version is deprecated
                .execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                default:
                    throw new RequestException("Could not create an OpenTok Session. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not create an OpenTok Session", e);
        }
    }

    public String signal(String sessionId, String connectionId, SignalProperties properties) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + (connectionId != null && connectionId.length() > 0 ? "/connection/" + connectionId : "") + "/signal";
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
        Future<Response> request = this.preparePost(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not send a signal.", e);
        }
    }

    public String getArchive(String archiveId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId;
        Future<Response> request = this.prepareGet(url)
                .setHeader("Accept", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
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

        Future<Response> request = this.prepareGet(url)
                .setHeader("Accept", "application/json")
                .execute();

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
                    throw new RequestException("Could not get OpenTok Archives. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException e) {
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
        if (properties.getMultiArchiveTag() != null) {
            requestJson.put("multiArchiveTag", properties.getMultiArchiveTag());
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
                .setHeader("Accept", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not start an OpenTok Archive.", e);
        }
    }

    public String stopArchive(String archiveId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId + "/stop";
        Future<Response> request = this.preparePost(url)
                .setHeader("Accept", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not stop an OpenTok Archive.", e);
        }
    }

    public String deleteArchive(String archiveId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId;
        Future<Response> request = this.prepareDelete(url).execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
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

        Future<Response> request = this.preparePatch(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .execute();

        try {
            Response response = request.get();
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

        } catch (ExecutionException | InterruptedException e) {
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
        Future<Response> request = this.preparePut(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
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

        Future<Response> request = this.preparePut(url)
                .setBody(outputStream.toString())
                .setHeader("Content-Type", "application/json")
                .execute();
        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
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

        Future<Response> request = this.preparePost(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not start an OpenTok Broadcast.", e);
        }
    }

    public String stopBroadcast(String broadcastId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId + "/stop";

        Future<Response> request = this.preparePost(url)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not start an OpenTok Broadcast.", e);
        }
    }

    public String getBroadcast(String broadcastId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId;

        Future<Response> request = this.prepareGet(url)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
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

        Future<Response> request = this.preparePatch(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .execute();
        try {
            Response response = request.get();
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

        } catch (ExecutionException | InterruptedException e) {
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
        Future<Response> request = this.preparePut(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not set the layout, broadcastId = " + broadcastId, e);
        }
    }

    public String forceDisconnect(String sessionId, String connectionId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/connection/" + connectionId;
        Future<Response> request = this.prepareDelete(url).execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
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

        Future<Response> request = this.preparePost(url)
                .setBody(outputStream.toString())
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
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
        Future<Response> request = this.preparePost(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                default:
                    throw new RequestException("Could not get a proper response. response code: " +
                            response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException e) {
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
        Future<Response> request = this.prepareGet(url).execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException(response.getResponseBody());
                case 403:
                    throw new RequestException("Invalid OpenTok API key or JWT token.");
                case 500:
                    throw new RequestException("OpenTok server error.");
                default:
                    throw new RequestException("Could not get stream information. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not get stream information", e);
        }
    }

    public String forceMuteStream(String sessionId, String streamId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/stream/" + streamId + "/mute";
        Future<Response> request = this.preparePost(url).execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 400:
                    throw new RequestException(response.getResponseBody());
                case 403:
                    throw new RequestException("Invalid OpenTok API key or JWT token.");
                case 500:
                    throw new RequestException("OpenTok server error.");
                default:
                    throw new RequestException("Could not mute stream. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException e) {
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

        Future<Response> request = this.preparePost(url)
                .setBody(outputStream.toString())
                .setHeader("Content-Type", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
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

        Future<Response> request = this.preparePost(url)
                .setBody(outputStream.toString())
                .setHeader("Content-Type", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not get stream information", e);
        }
    }

    public String listStreams(String sessionId) throws RequestException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/stream";
        Future<Response> request = this.prepareGet(url)
                .setHeader("Accept", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not get streams information", e);
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

        Future<Response> request = preparePost(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException ex) {
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

        Future<Response> request = this.preparePost(url)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .setBody(outputStream.toString())
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not start render", e);
        }
    }

    public String getRender(String renderId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/render/" + renderId;

        Future<Response> request = this.prepareGet(url)
                .setHeader("Accept", "application/json")
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not get render '"+renderId+"'", e);
        }
    }

    public void stopRender(String renderId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/render/" + renderId;
        try {
            Response response = this.prepareDelete(url).execute().get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not stop render", e);
        }
    }

    public String listRenders(Integer offset, Integer count) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/render";

        BoundRequestBuilder rqBuilder = this.prepareGet(url).setHeader("Accept", "application/json");
        if (offset != null) {
            rqBuilder.addQueryParam("offset", offset.toString());
        }
        if (count != null) {
            rqBuilder.addQueryParam("count", count.toString());
        }

        try {
            Response response = rqBuilder.execute().get();
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
        } catch (InterruptedException | ExecutionException e) {
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

        Future<Response> request = this.preparePost(url)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .setBody(outputStream.toString())
                .execute();

        try {
            Response response = request.get();
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not stop captions", e);
        }
    }

    public void stopCaption(String captionsId) throws OpenTokException {
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/captions/" + captionsId + "/stop";
        try {
            Response response = this.preparePost(url).execute().get();
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
        } catch (InterruptedException | ExecutionException e) {
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
        private final int apiKey;
        private final String apiSecret;
        private Proxy proxy;
        private ProxyAuthScheme proxyAuthScheme;
        private String principal;
        private String password;
        private String apiUrl;
        private String userAgent = DefaultUserAgent.DEFAULT_USER_AGENT;
        private AsyncHttpClientConfig config;
        private int requestTimeoutMS;

        public Builder(int apiKey, String apiSecret) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
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
            DefaultAsyncHttpClientConfig.Builder configBuilder = new DefaultAsyncHttpClientConfig.Builder()
                    .setUserAgent(userAgent)
                    .addRequestFilter(new TokenAuthRequestFilter(apiKey, apiSecret));

            if (apiUrl == null) {
                apiUrl = DefaultApiUrl.DEFAULT_API_URI;
            }
            if (proxy != null) {
                configBuilder.setProxyServer(createProxyServer(proxy, proxyAuthScheme, principal, password));
            }
            if (requestTimeoutMS != 0) {
                configBuilder.setRequestTimeout(requestTimeoutMS);
            }

            config = configBuilder.build();
            // NOTE: not thread-safe, config could be modified by another thread here?
            return new HttpClient(this);
        }

        // credit: https://github.com/AsyncHttpClient/async-http-client/blob/b52a8de5d6a862b5d1652d62f87ce774cbcff156/src/main/java/com/ning/http/client/ProxyServer.java#L99-L127
        static ProxyServer createProxyServer(final Proxy proxy, ProxyAuthScheme proxyAuthScheme, String principal, String password) {
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
            ProxyServer.Builder builder = new ProxyServer.Builder(isaHost, isa.getPort());

            if (principal != null) {
                Realm.AuthScheme authScheme = null;
                switch (proxyAuthScheme) {
                    case BASIC:
                        authScheme = AuthScheme.BASIC;
                        break;
                    case DIGEST:
                        authScheme = AuthScheme.DIGEST;
                        break;
                    case NTLM:
                        authScheme = AuthScheme.NTLM;
                        break;
                    case KERBEROS:
                        authScheme = AuthScheme.KERBEROS;
                        break;
                    case SPNEGO:
                        authScheme = AuthScheme.SPNEGO;
                        break;
                }

                Realm.Builder rb = new Realm.Builder(principal, password);
                rb.setScheme(authScheme);

                builder.setRealm(rb.build());
            }

            return builder.build();
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

        @Override
        public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {
            try {
                return new FilterContext.FilterContextBuilder<>(ctx)
                        .request(ctx.getRequest().toBuilder()
                            .addHeader(authHeader, TokenGenerator.generateToken(apiKey, apiSecret))
                            .build()
                        )
                        .build();
            } catch (OpenTokException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
