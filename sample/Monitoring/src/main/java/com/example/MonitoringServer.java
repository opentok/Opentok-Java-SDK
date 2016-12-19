package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static spark.Spark.*;
import spark.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import com.opentok.*;
import com.opentok.exception.OpenTokException;

public class MonitoringServer {

    private static final String apiKey = System.getProperty("API_KEY");
    private static final String apiSecret = System.getProperty("API_SECRET");
    private static final String publicUrl = System.getProperty("PUBLIC_URL");

    private static OpenTok opentok;
    private static String sessionId;

    private static List<Map<String, String> > events = new ArrayList<Map<String, String> >();

    public static void main(String[] args) throws OpenTokException {

        if (apiKey == null || apiKey.isEmpty() || apiSecret == null || apiSecret.isEmpty() || publicUrl == null || publicUrl.isEmpty()) {
            System.out.println("You must define API_KEY, API_SECRET and PUBLIC_URL system properties in the build.gradle file.");
            System.exit(-1);
        }

        opentok = new OpenTok(Integer.parseInt(apiKey), apiSecret);

        sessionId = opentok.createSession(new SessionProperties.Builder()
                .mediaMode(MediaMode.ROUTED)
                .build())
                .getSessionId();

        System.out.println("Registering callbacks");
        String url = publicUrl + "/callback";
        opentok.registerCallback(CallbackGroup.CONNECTION, CallbackEvent.CREATED, url);
        opentok.registerCallback(CallbackGroup.CONNECTION, CallbackEvent.DESTROYED, url);
        opentok.registerCallback(CallbackGroup.STREAM, CallbackEvent.CREATED, url);
        opentok.registerCallback(CallbackGroup.STREAM, CallbackEvent.DESTROYED, url);
        System.out.println("Callbacks registered");

        externalStaticFileLocation("./public");

        get(new FreeMarkerTemplateView("/") {
            @Override
            public ModelAndView handle(Request request, Response response) {
                String token = null;
                try {
                    token = opentok.generateToken(sessionId, new TokenOptions.Builder()
                        .role(Role.MODERATOR)
                        .build());
                } catch (OpenTokException e) {
                    e.printStackTrace();
                }

                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("apiKey", apiKey);
                attributes.put("sessionId", sessionId);
                attributes.put("token", token);

                return new ModelAndView(attributes, "index.ftl");
            }
        });

        get(new FreeMarkerTemplateView("/events") {
            @Override
            public ModelAndView handle(Request request, Response response) {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("events", events);
                return new ModelAndView(attributes, "events.ftl");
            }
        });

        post(new Route("/callback") {
            @Override
            public Object handle(Request request, Response response) {
                String body = request.body();

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> obj = mapper.readValue(body, new TypeReference<HashMap>(){});

                    events.add(0, obj);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        });

        post(new Route("/disconnect") {
            @Override
            public Object handle(Request request, Response response) {
                HttpServletRequest req = request.raw();
                String sessionId = req.getParameter("sessionId");
                String connectionId = req.getParameter("connectionId");

                try {
                    opentok.forceDisconnect(sessionId, connectionId);
                } catch (OpenTokException e) {
                    e.printStackTrace();
                }

                return null;
            }
        });

        post(new Route("/signal") {
            @Override
            public Object handle(Request request, Response response) {
                HttpServletRequest req = request.raw();
                String sessionId = req.getParameter("sessionId");
                String connectionId = req.getParameter("connectionId");

                Signal signal = new Signal();
                signal.setType("chat");
                signal.setData("Hello!");

                try {
                    opentok.signal(sessionId, connectionId, signal);
                } catch (OpenTokException e) {
                    e.printStackTrace();
                }

                return null;
            }
        });
    }
}
