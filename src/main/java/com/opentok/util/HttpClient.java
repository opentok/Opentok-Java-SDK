/**
 * OpenTok Java SDK
 * Copyright (C) 2019 TokBox, Inc.
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
import com.opentok.ArchiveLayout;
import com.opentok.ArchiveProperties;
import com.opentok.BroadcastLayout;
import com.opentok.BroadcastProperties;
import com.opentok.RtmpProperties;
import com.opentok.SignalProperties;
import com.opentok.SipProperties;
import com.opentok.StreamListProperties;
import com.opentok.StreamProperties;
import com.opentok.constants.DefaultApiUrl;
import com.opentok.constants.Version;
import com.opentok.exception.InvalidArgumentException;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.RequestException;
import org.apache.commons.lang.StringUtils;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Realm;
import org.asynchttpclient.Realm.AuthScheme;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.FilterException;
import org.asynchttpclient.filter.RequestFilter;
import org.asynchttpclient.proxy.ProxyServer;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
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

    public String createSession(Map<String, Collection<String>> params) throws RequestException {
        String responseString = null;
        Response response = null;
        Map<String, List<String>> paramsWithList = null;
        if (params != null) {
            paramsWithList = new HashMap<>();
            for (Entry<String, Collection<String>> entry : params.entrySet()) {
                paramsWithList.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }

        Future<Response> request = this.preparePost(this.apiUrl + "/session/create")
                .setFormParams(paramsWithList)
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not create an OpenTok Session", e);
        }
        return responseString;
    }

    public String signal(String sessionId, String connectionId, SignalProperties properties) throws   OpenTokException , RequestException {
        String responseString = null;
        String requestBody = null;

        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + (connectionId != null && connectionId.length() > 0 ? "/connection/"+ connectionId : "") +  "/signal";
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();

        requestJson.put("type", properties.type());
        requestJson.put("data", properties.data());

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
                    responseString = response.getResponseBody();
                    break;
                case 400:
                    throw new RequestException("Could not send a signal. One of the signal properties is invalid.");
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not  get an OpenTok Archive", e);
        }

        return responseString;
    }

    public String getArchives(String sessionId, int offset, int count) throws OpenTokException {
        if(offset < 0 || count < 0 || count > 1000)  {
            throw new InvalidArgumentException("Make sure count parameter value is >= 0 and/or offset parameter value is <=1000");
        }

        String responseString;
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
        if(sessionId != null && !sessionId.isEmpty())  {
            url += (url.contains("?") ? "&" : "?") + "sessionId=" + sessionId;
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not get OpenTok Archives", e);
        }

        return responseString;
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
        if(properties.layout() != null) {
            ObjectNode layout = requestJson.putObject("layout");
            layout.put("type", properties.layout().getType().toString());
            layout.put("stylesheet", properties.layout().getStylesheet());
        }
        if (properties.name() != null) {
            requestJson.put("name", properties.name());
        }
        if (properties.resolution() != null) {
            requestJson.put("resolution", properties.resolution());
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
                case 400:
                    throw new RequestException("Could not start an OpenTok Archive. A bad request, check input archive properties like resolution etc.");
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
        } catch (InterruptedException | ExecutionException e) {
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
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not delete an OpenTok Archive. archiveId = " + archiveId, e);
        }

        return responseString;
    }

    public String setArchiveLayout(String archiveId, ArchiveProperties properties) throws OpenTokException {
        if(properties.layout() == null) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        String type = properties.layout().getType().toString();
        String stylesheet = properties.layout().getStylesheet();
        if(StringUtils.isEmpty(type)) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        if ((type.equals(ArchiveLayout.Type.CUSTOM.toString()) && StringUtils.isEmpty(stylesheet)) ||
            (!type.equals(ArchiveLayout.Type.CUSTOM.toString()) && !StringUtils.isEmpty(stylesheet))) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        String responseString = null;
        String requestBody = null;
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/archive/" + archiveId + "/layout";
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();
        requestJson.put("type", type);
        if(type.equals(ArchiveLayout.Type.CUSTOM.toString())) {
            requestJson.put("stylesheet", properties.layout().getStylesheet());
        }

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
                    responseString = response.getResponseBody();
                    break;
                case 400:
                    throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
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
        return responseString;
    }

    public String setStreamLayouts(String sessionId, StreamListProperties properties) throws OpenTokException {
        String responseString = null;
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

            for(StreamProperties stream : properties.getStreamList()) {
                jGenerator.writeStartObject();
                jGenerator.writeFieldName("id");
                jGenerator.writeString(stream.id());
                jGenerator.writeFieldName("layoutClassList");
                List<String> stringList = stream.getLayoutClassList();
                StringJoiner sj = new StringJoiner(",");
                stringList.stream().forEach(e -> sj.add(doubleQuotes + e + doubleQuotes));
                jGenerator.writeRawValue("["+ sj.toString() + "]");
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
                    responseString = response.getResponseBody();
                    break;
                case 400:
                    throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
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
        return responseString;
    }

    public String startBroadcast(String sessionId, BroadcastProperties properties)
            throws OpenTokException {
        String responseString = null;
        String requestBody = null;
       
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/broadcast";

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();
        requestJson.put("sessionId", sessionId);
        if(properties.layout() != null) {
            ObjectNode layout = requestJson.putObject("layout");
            String type = properties.layout().getType().toString();
            layout.put("type", type);
            if(type.equals(BroadcastLayout.Type.CUSTOM.toString())) {
                requestJson.put("stylesheet", properties.layout().getStylesheet());
            }
        }
        if (properties.maxDuration() > 0) {
            requestJson.put("maxDuration", properties.maxDuration());
        }
        if (properties.resolution() != null) {
            requestJson.put("resolution", properties.resolution());
        }
        ObjectNode outputs = requestJson.putObject("outputs");
        if(properties.hasHls()) {
            outputs.put("hls", nodeFactory.objectNode());
        }
        ArrayNode rtmp = outputs.putArray("rtmp");
        for (RtmpProperties prop : properties.getRtmpList()) {
            ObjectNode rtmpProps = nodeFactory.objectNode();
            rtmpProps.put("id", prop.id());
            rtmpProps.put("serverUrl", prop.serverUrl());
            rtmpProps.put("streamName", prop.streamName());
            rtmp.add(rtmpProps);
        }

        try {
            requestBody = new ObjectMapper().writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not start an OpenTok Broadcast. The JSON body encoding failed.", e);
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
                case 400:
                    throw new RequestException("Could not start an OpenTok Broadcast. A bad request, check input  properties like resolution etc.");
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
        return responseString;
    }

    public String stopBroadcast(String broadcastId)
            throws OpenTokException {
        String responseString = null;
        String requestBody = null;
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId + "/stop";
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
                case 400:
                    throw new RequestException("Could not start an OpenTok Broadcast. A bad request, check input  properties like resolution etc.");
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
        return responseString;
    }

    public String getBroadcast(String broadcastId)
            throws OpenTokException {
        String responseString = null;
        String requestBody = null;
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId;
        Future<Response> request = this.prepareGet(url)
                .setBody(requestBody)
                .setHeader("Content-Type", "application/json")
                .execute();
        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    responseString = response.getResponseBody();
                    break;
                case 400:
                    throw new RequestException("Could not get Broadcast stream information. A bad request, check input  properties.");
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
        return responseString;
    }
    public String setBroadcastLayout(String broadcastId, BroadcastProperties properties) throws OpenTokException {
        if(properties.layout() == null) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        String type = properties.layout().getType().toString();
        String stylesheet = properties.layout().getStylesheet();
        if(StringUtils.isEmpty(type)) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        if ((type.equals(BroadcastLayout.Type.CUSTOM.toString()) && StringUtils.isEmpty(stylesheet)) ||
                (!type.equals(BroadcastLayout.Type.CUSTOM.toString()) && !StringUtils.isEmpty(stylesheet))) {
            throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
        }
        String responseString = null;
        String requestBody = null;
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/broadcast/" + broadcastId + "/layout";
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode requestJson = nodeFactory.objectNode();
        requestJson.put("type", type);
        if(type.equals(BroadcastLayout.Type.CUSTOM.toString())) {
            requestJson.put("stylesheet", properties.layout().getStylesheet());
        }

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
                    responseString = response.getResponseBody();
                    break;
                case 400:
                    throw new RequestException("Could not set the layout. Either an invalid JSON or an invalid layout options.");
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
        return responseString;
    }

    public String forceDisconnect(String sessionId, String connectionId) throws   OpenTokException , RequestException {
        String responseString = null;
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/connection/"+ connectionId ;
        Future<Response> request = this.prepareDelete(url).execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 204:
                    responseString = response.getResponseBody();
                    break;
                case 400:
                    throw new RequestException("Could not force disconnect. One of the arguments — sessionId or connectionId — is invalid.");
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

        return responseString;
    }
    public String sipDial(String sessionId, String token, SipProperties props) throws OpenTokException {
        String responseString = null;
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
            if(!StringUtils.isEmpty(props.from())) {
                jGenerator.writeFieldName("from");
                jGenerator.writeRawValue(dQuotes + props.from() + dQuotes);
            }
            if(!StringUtils.isEmpty(props.headersJsonStartingWithXDash())) {
                jGenerator.writeFieldName("headers");
                jGenerator.writeRawValue(props.headersJsonStartingWithXDash());
            }
            if(!StringUtils.isEmpty(props.userName()) && !StringUtils.isEmpty(props.password())) {
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
                .execute();
        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    responseString = response.getResponseBody();
                    break;
                case 400:
                    throw new RequestException("Could not set the sip dial. Either an invalid sessionId or the custom header does not start with the X- prefix.");
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
        return responseString;
    }
    public String getStream(String sessionId, String streamId) throws RequestException {
        String responseString = null;
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/stream/" + streamId;
        Future<Response> request = this.prepareGet(url).execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    responseString = response.getResponseBody();
                    break;
                case 400:
                    throw new RequestException("Invalid request. This response may indicate that data in your request data is invalid JSON. Or it may indicate that you do not pass in a session ID or you passed in an invalid stream ID. "
                           + "sessionId: " + sessionId +  "streamId: " + streamId);
                case 403:
                    throw new RequestException("Invalid OpenTok API key or JWT token.");

                case 408:
                    throw new RequestException("You passed in an invalid stream ID." +
                            "streamId: " + streamId);
                case 500:
                    throw new RequestException("OpenTok server error.");
                default:
                    throw new RequestException("Could not get stream information. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RequestException("Could not get stream information", e);
        }

        return responseString;
    }

    public String listStreams(String sessionId) throws RequestException {
        String responseString = null;
        String url = this.apiUrl + "/v2/project/" + this.apiKey + "/session/" + sessionId + "/stream" ;
        Future<Response> request = this.prepareGet(url).execute();

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    responseString = response.getResponseBody();
                    break;
                case 400:
                    throw new RequestException("Invalid request. This response may indicate that data in your request data is invalid JSON. Or it may indicate that you do not pass in a session ID or you passed in an invalid stream ID" );
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

        return responseString;
    }
    public static enum ProxyAuthScheme {
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

        public HttpClient build() {
            DefaultAsyncHttpClientConfig.Builder configBuilder = new DefaultAsyncHttpClientConfig.Builder()
                    .setUserAgent("Opentok-Java-SDK/" + Version.VERSION + " JRE/" + System.getProperty("java.version"))
                    .addRequestFilter(new TokenAuthRequestFilter(this.apiKey, this.apiSecret));
            if (this.apiUrl == null) {
                this.apiUrl=DefaultApiUrl.DEFAULT_API_URI;
            }
            
            if (this.proxy != null) {
                configBuilder.setProxyServer(createProxyServer(this.proxy, this.proxyAuthScheme, this.principal, this.password));
            }
            
            this.config = configBuilder.build();
            // NOTE: not thread-safe, config could be modified by another thread here?
            HttpClient client = new HttpClient(this);
            return client;
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
                return new FilterContext.FilterContextBuilder<T>(ctx)
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
