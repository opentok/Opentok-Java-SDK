package com.example;

import static spark.Spark.*;

import com.opentok.*;
import spark.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.opentok.exception.OpenTokException;

public class ArchivingServer {

    private static final String apiKey = System.getProperty("API_KEY");
    private static final String apiSecret = System.getProperty("API_SECRET");
    private static OpenTok opentok;
    private static String sessionId;

    public static void main(String[] args) throws OpenTokException {

        if (apiKey == null || apiKey.isEmpty() || apiSecret == null || apiSecret.isEmpty()) {
            System.out.println("You must define API_KEY and API_SECRET system properties in the build.gradle file.");
            System.exit(-1);
        }

        opentok = new OpenTok(Integer.parseInt(apiKey), apiSecret);

        sessionId = opentok.createSession(new SessionProperties.Builder()
                .mediaMode(MediaMode.ROUTED)
                .build())
                .getSessionId();

        externalStaticFileLocation("./public");

        get(new FreeMarkerTemplateView("/") {
            @Override
            public ModelAndView handle(Request request, Response response) {
                //Map<String, Object> attributes = new HashMap<String, Object>();
                return new ModelAndView(null, "index.ftl");
            }
        });

        get(new FreeMarkerTemplateView("/host") {
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

                return new ModelAndView(attributes, "host.ftl");
            }
        });

        get(new FreeMarkerTemplateView("/participant") {
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

                return new ModelAndView(attributes, "participant.ftl");
            }
        });

        get(new FreeMarkerTemplateView("/history") {
            @Override
            public ModelAndView handle(Request request, Response response) {

                String pageParam = request.queryParams("page");
                int page;
                try {
                    page = Integer.parseInt(pageParam);
                } catch (NumberFormatException e) {
                    page = 1;
                }

                int offset = (page - 1) * 5;
                List<Archive> archives = null;
                try {
                    archives = opentok.listArchives(offset, 5);
                } catch (OpenTokException e) {
                    e.printStackTrace();
                }

                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("archives", archives);
                attributes.put("showPrevious", null);
                // TODO: we don't have a total count, how do we know if there is a next page?
                attributes.put("showNext", "/history?page=" + (page + 1));

                if (page > 1) {
                    attributes.put("showPrevious", "/history?page=" + (page - 1));
                }

                return new ModelAndView(attributes, "history.ftl");
            }
        });

        get(new Route("/download/:archiveId") {
            @Override
            public Object handle(Request request, Response response) {

                Archive archive = null;
                try {
                    archive = opentok.getArchive(request.params("archiveId"));
                } catch (OpenTokException e) {
                    e.printStackTrace();
                    return null;
                }

                response.redirect(archive.getUrl());
                return null;
            }
        });

        get(new Route("/start") {
            @Override
            public Object handle(Request request, Response response) {

                Archive archive = null;
                try {
                    archive = opentok.startArchive(sessionId, "Java Archiving Sample App");
                } catch (OpenTokException e) {
                    e.printStackTrace();
                    return null;
                }
                return archive.toString();
            }
        });

        get(new Route("/stop/:archiveId") {
            @Override
            public Object handle(Request request, Response response) {

                Archive archive = null;
                try {
                    archive = opentok.stopArchive(request.params("archiveId"));
                } catch (OpenTokException e) {
                    e.printStackTrace();
                    return null;
                }
                return archive.toString();
            }
        });

        get(new Route("/delete/:archiveId") {
            @Override
            public Object handle(Request request, Response response) {

                try {
                    opentok.deleteArchive(request.params("archiveId"));
                } catch (OpenTokException e) {
                    e.printStackTrace();
                    return null;
                }
                response.redirect("/history");
                return null;
            }
        });


    }
}
