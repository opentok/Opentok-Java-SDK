package com.example;

import static spark.Spark.*;
import spark.*;

import java.util.Map;
import java.util.HashMap;

import com.opentok.OpenTok;
import com.opentok.exception.OpenTokException;

public class HelloWorldServer {

    private static final String apiKey = System.getProperty("API_KEY");
    private static final String apiSecret = System.getProperty("API_SECRET");
    private static final OpenTok opentok = new OpenTok(Integer.parseInt(apiKey), apiSecret);
    private static String sessionId;

    public static void main(String[] args) throws OpenTokException {

        if (apiKey == null || apiKey.isEmpty() || apiSecret == null || apiSecret.isEmpty()) {
            System.out.println("You must define API_KEY and API_SECRET system properties in the build.gradle file.");
            System.exit(-1);
        }

        sessionId = opentok.createSession().getSessionId();

        externalStaticFileLocation("./public");

        get(new FreeMarkerTemplateView("/") {
            @Override
            public ModelAndView handle(Request request, Response response) {

                String token = null;
                try {
                    token = opentok.generateToken(sessionId);
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

    }
}
