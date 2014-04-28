package com.opentok.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.*;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.FilterException;
import com.ning.http.client.filter.RequestFilter;

import com.opentok.constants.Version;

public class HttpClient extends AsyncHttpClient {
    
    private final String apiUrl;
    private final int apiKey;

    private HttpClient(Builder builder) {
        super(builder.config);
        this.apiKey = builder.apiKey;
        this.apiUrl = builder.apiUrl;
    }

    public String createSession(Map<String, Collection<String>> params) {
        Future<Response> request = null;
        String responseString = null;
        Response response = null;
        FluentStringsMap paramsString = new FluentStringsMap().addAll(params);

        try {
            request = this.preparePost(this.apiUrl + "/session/create")
                    .setParameters(paramsString)
                    .execute();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }

        try {
            response = request.get();
            // TODO: check response code
            responseString = response.getResponseBody();
        } catch (InterruptedException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }
        return responseString;
    }

    public String getArchive(String archiveId) {
        String responseString = null;
        Future<Response> request = null;
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive/" + archiveId;

        try {
            request = this.prepareGet(url).execute();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }

        try {
            Response response = request.get();
            // TODO: check response code
            responseString = response.getResponseBody();
        } catch (InterruptedException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }

        return responseString;
    }

    public String getArchives(int offset, int count) {
        String responseString = null;
        Future<Response> request = null;
        // TODO: maybe use a StringBuilder?
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive";
        if (offset != 0 || count != 1000) {
            url += "?";
            if (offset != 0) {
                url += ("offset=" + Integer.toString(offset));
            }
            if (count != 1000) {
                url += ("count=" + Integer.toString(count));
            }
        }

        try {
            request = this.prepareGet(url).execute();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }

        try {
            Response response = request.get();
            // TODO: check response code
            responseString = response.getResponseBody();
        } catch (InterruptedException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }

        return responseString;
    }

    public String startArchive(String sessionId, String name) {
        String responseString = null;
        Future<Response> request = null;
        String requestBody = null;
        // TODO: maybe use a StringBuilder?
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive";

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> jsonBody = new HashMap<String, String>();
        jsonBody.put("sessionId", sessionId);
        if (name != null) {
            jsonBody.put("name", name);
        }
        try {
            requestBody = mapper.writeValueAsString(jsonBody);
        } catch (JsonProcessingException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }
        try {
            request = this.preparePost(url)
                    .setBody(requestBody)
                    .setHeader("Content-Type", "application/json")
                    .execute();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }

        try {
            Response response = request.get();
            // TODO: check response code
            responseString = response.getResponseBody();
        } catch (InterruptedException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }
        return responseString;
    }

    public String stopArchive(String archiveId) {
        String responseString = null;
        Future<Response> request = null;
        // TODO: maybe use a StringBuilder?
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive/stop";

        try {
            request = this.preparePost(url).execute();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }

        try {
            Response response = request.get();
            // TODO: check response code
            responseString = response.getResponseBody();
        } catch (InterruptedException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }
        return responseString;
    }

    public String deleteArchive(String archiveId) {
        String responseString = null;
        Future<Response> request = null;
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive/" + archiveId;

        try {
            request = this.prepareDelete(url).execute();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }

        try {
            Response response = request.get();
            // TODO: check response code
            responseString = response.getResponseBody();
        } catch (InterruptedException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: throw OpenTokException
            e.printStackTrace();
        }

        return responseString;
    }

    public static class Builder {
        private final int apiKey;
        private final String apiSecret;
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

        public HttpClient build() {
            this.config = new AsyncHttpClientConfig.Builder()
                    .setUserAgent("Opentok-Java-SDK/"+Version.VERSION)
                    .addRequestFilter(new PartnerAuthRequestFilter(this.apiKey, this.apiSecret))
                    .build();
            // NOTE: not thread-safe, config could be modified by another thread here?
            HttpClient client = new HttpClient(this);
            return client;
        }
    }

    static class PartnerAuthRequestFilter implements RequestFilter {

        private int apiKey;
        private String apiSecret;

        public PartnerAuthRequestFilter(int apiKey, String apiSecret) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
        }

        public FilterContext filter(FilterContext ctx) throws FilterException {
            return new FilterContext.FilterContextBuilder(ctx)
                    .request(new RequestBuilder(ctx.getRequest())
                            .addHeader("X-TB-PARTNER-AUTH", this.apiKey+":"+this.apiSecret)
                            .build())
                    .build();
        }
    }
}
