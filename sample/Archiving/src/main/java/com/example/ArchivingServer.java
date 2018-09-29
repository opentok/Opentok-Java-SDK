package com.example;

import static spark.Spark.*;

import com.opentok.*;
import com.opentok.Archive.OutputMode;
import com.opentok.ArchiveLayout;
import com.opentok.ArchiveProperties;

import spark.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.opentok.exception.OpenTokException;

public class ArchivingServer {

    private static final String apiKey = System.getProperty("API_KEY");
    private static final String apiSecret = System.getProperty("API_SECRET");
    private static OpenTok opentok;
    private static String sessionId;
    private static String focusStreamId = "";
    private static String layoutType = "horizontalPresentation";

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
                ArrayList<String> layoutClassList = new ArrayList<String>();
                layoutClassList.add("focus");
                try {
                    token = opentok.generateToken(sessionId, new TokenOptions.Builder()
                        .role(Role.MODERATOR)
                        .initialLayoutClassList(layoutClassList)
                        .build());
                } catch (OpenTokException e) {
                    e.printStackTrace();
                }

                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("apiKey", apiKey);
                attributes.put("sessionId", sessionId);
                attributes.put("token", token);
                attributes.put("layout", layoutType);
                attributes.put("focusStreamId", focusStreamId);

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
                attributes.put("layout", layoutType);
                attributes.put("focusStreamId", focusStreamId);

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

        post(new Route("/start") {
            @Override
            public Object handle(Request request, Response response) {

                Archive archive = null;
                HttpServletRequest req = request.raw();
                boolean hasAudio = req.getParameterMap().containsKey("hasAudio");
                boolean hasVideo = req.getParameterMap().containsKey("hasVideo");
                OutputMode outputMode = OutputMode.INDIVIDUAL;
                ArchiveLayout layout = null;
                if (req.getParameter("outputMode").equals("composed")) {
                    outputMode = OutputMode.COMPOSED;
                    layout = new ArchiveLayout(ArchiveLayout.Type.HORIZONTAL);
                }
                try {
                    ArchiveProperties properties = new ArchiveProperties.Builder()
                                            .name("Java Archiving Sample App")
                                            .hasAudio(hasAudio)
                                            .hasVideo(hasVideo)
                                            .outputMode(outputMode)
                                            .layout(layout)
                                            .build();
                    archive = opentok.startArchive(sessionId, properties);
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

        post(new Route("/archive/:archiveId/layout") {
            @Override
            public Object handle(Request request, Response response) {
                try {
                    HttpServletRequest req = request.raw();
                    String layoutType = req.getParameterMap().get("type")[0];
                    ArchiveLayout.Type type = ArchiveLayout.Type.HORIZONTAL;
                    if (layoutType.equals("verticalPresentation")) {
                        type = ArchiveLayout.Type.VERTICAL;
                    }
                    ArchiveProperties archiveProperties = new ArchiveProperties.Builder()
                        .layout(new ArchiveLayout(type))
                        .build();
                    opentok.setArchiveLayout(request.params("archiveId"), archiveProperties);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.status(400);
                    return e.getMessage();
                }
                return layoutType;
            }
        });

        post(new Route("/focus") {
            @Override
            public Object handle(Request request, Response response) {
                ArrayList<String> otherStreams = new ArrayList<String>();
                HttpServletRequest req = request.raw();
                String newFocusStreamId = req.getParameterMap().get("focus")[0];

                if (newFocusStreamId.equals(focusStreamId)) {
                  return focusStreamId;
                }

                if (focusStreamId.isEmpty()) {
                  focusStreamId = newFocusStreamId;
                  return focusStreamId;
                }

                try {
                    StreamProperties newFocusStreamProperties = new StreamProperties.Builder()
                        .id(newFocusStreamId)
                        .addLayoutClass("focus")
                        .build();
                    StreamProperties oldFocusStreamProperties = new StreamProperties.Builder()
                        .id(focusStreamId)
                        .build();
                    StreamListProperties streamListProperties = new StreamListProperties.Builder()
                        .addStreamProperties(newFocusStreamProperties)
                        .addStreamProperties(oldFocusStreamProperties)
                        .build();
                    opentok.setStreamLayouts(sessionId, streamListProperties);
                    focusStreamId = newFocusStreamId;
                } catch (Exception e) {
                    e.printStackTrace();
                    response.status(400);
                    return e.getMessage();
                }
                return focusStreamId;
            }
        });
    }
}
