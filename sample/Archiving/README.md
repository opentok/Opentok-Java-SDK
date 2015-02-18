# OpenTok Archiving Sample for Java

This is a simple demo app that shows how you can use the OpenTok Java SDK to archive (or record)
Sessions, list archives that have been created, download the recordings, and delete the recordings.

## Running the App

First, add your own API Key and API Secret to the system properties. For your convenience, the
`build.gradle` file is set up for you to place your values into it.

```
run.systemProperty 'API_KEY', '000000'
run.systemProperty 'API_SECRET', 'abcdef1234567890abcdef01234567890abcdef'
```

Next, start the server using Gradle (which handles dependencies and setting up the environment).

```
$ gradle :sample/Archiving:run
```

Or if you are using the Gradle Wrapper that is distributed with the project, from the root project
directory:

```
$ ./gradlew :sample/Archiving:run
```

Visit <http://localhost:4567> in your browser. You can now create new archives (either as a host or
as a participant) and also play archives that have already been created.


## Walkthrough

This demo application uses the same frameworks and libraries as the HelloWorld sample. If you have
not already gotten familiar with the code in that project, consider doing so before continuing.

The explanations below are separated by page. Each section will focus on a route handler within the
main application (src/main/java/com/example/ArchivingServer.java).

### Creating Archives – Host View

Start by visiting the host page at <http://localhost:4567/host> and using the application to record
an archive. Your browser will first ask you to approve permission to use the camera and microphone.
Once you've accepted, your image will appear inside the section titled 'Host'. To start recording
the video stream, press the 'Start Archiving' button. Once archiving has begun the button will turn
green and change to 'Stop Archiving'. You should also see a red blinking indicator that you are
being recorded. Wave and say hello! Stop archiving when you are done.

Next we will see how the host view is implemented on the server. The route handler for this page is
shown below:

```java
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
```

If you've completed the HelloWorld walkthrough, this should look familiar. This handler simply
generates the three strings that the client (JavaScript) needs to connect to the session: `apiKey`,
`sessionId` and `token`. After the user has connected to the session, they press the
'Start Archiving' button, which sends an XHR (or Ajax) request to the <http://localhost:4567/start>
URL. The route handler for this URL is shown below:

```java
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
```

In this handler, the `startArchive()` method of the `opentok` instance is called with the `sessionId`
for the session that needs to be archived. The optional second argument is `name`, which is stored with
the archive and can be read later. In this case, as in the HelloWorld sample app, there is
only one session created and it is used here and for the participant view. This will trigger the
recording to begin. The response sent back to the client's XHR request will be the JSON
representation of the archive, which is returned from the `toString()` method. The client is also
listening for the `archiveStarted` event, and uses that event to change the 'Start Archiving' button
to show 'Stop Archiving' instead. When the user presses the button this time, another XHR request
is sent to the <http://localhost:4567/stop/:archiveId> URL where `:archiveId` represents the ID the
client receives in the 'archiveStarted' event. The route handler for this request is shown below:

```java
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
```

This handler is very similar to the previous one. Instead of calling the `startArchive()` method,
the `stopArchive()` method is called. This method takes an `archiveId` as its parameter, which
is different for each time a session starts recording. But the client has sent this to the server
as part of the URL, so the `request.params("archiveId")` expression is used to retrieve it.

Now you have understood the three main routes that are used to create the Host experience of
creating an archive. Much of the functionality is done in the client with JavaScript. That code can
be found in the `public/js/host.js` file. Read about the
[OpenTok.js JavaScript](http://tokbox.com/opentok/libraries/client/js/) library to learn more.

### Creating Archives - Participant View

With the host view still open and publishing, open an additional window or tab and navigate to
<http://localhost:4567/participant> and allow the browser to use your camera and microphone. Once
again, start archiving in the host view. Back in the participant view, notice that the red blinking
indicator has been shown so that the participant knows his video is being recorded. Now stop the
archiving in the host view. Notice that the indicator has gone away in the participant view too.

Creating this view on the server is as simple as the HelloWorld sample application. See the code
for the route handler below:

```java
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
```

Since this view has no further interactivity with buttons, this is all that is needed for a client
that is participating in an archived session. Once again, much of the functionality is implemented
in the client, in code that can be found in the `public/js/participant.js` file.

### Past Archives

Start by visiting the history page at <http://localhost:4567/history>. You will see a table that
displays all the archives created with your API Key. If there are more than five, the older ones
can be seen by clicking the "Older →" link. If you click on the name of an archive, your browser
will start downloading the archive file. If you click the "Delete" link in the end of the row
for any archive, that archive will be deleted and no longer available. Some basic information like
when the archive was created, how long it is, and its status is also shown. You should see the
archives you created in the previous sections here.

We begin to see how this page is created by looking at the route handler for this URL:

```java
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
```

This view is paginated so that we don't potentially show hundreds of rows on the table, which would
be difficult for the user to navigate. So this code starts by figuring out which page needs to be
shown, where each page is a set of 5 archives. The `page` number is read from the request's query
string parameters as a string and then parsed into an `int`. The `offset`, which represents how many
archives are being skipped is always calculated as five times as many pages that are less than the current
page, which is `(page - 1) * 5`. Now there is enough information to ask for a list of archives from
OpenTok, which we do by calling the `listArchives()` method of the `opentok` instance. The first
parameter is the offset, and the second is the count (which is always 5 in this view). If we are not
at the first page, we can pass the view a string that contains the relative URL for the previous
page. Similarly, we can also include one for the next page. Now the application renders the view
using that information and the partial list of archives.

At this point the template file `src/main/resources/com/example/freemarker/history.ftl` handles
looping over the array of archives and outputting the proper information for each column in the
table. It also places a link to the download and delete routes around the archive's name and
its delete button, respectively.

The code for the download route handler is shown below:

```java
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
```

The download URL for an archive is available as a property of an `Archive` instance. In order to get
an instance to this archive, the `getArchive()` method of the `opentok` instance is used. The only
parameter it needs is the `archiveId`. We use the same technique as above to read that `archiveId`
from the URL. Lastly, we send a redirect response back to the browser so the download begins.

The code for the delete route handler is shown below:

```java
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
```

Once again the `archiveId` is retrieved from the URL of the request. This value is then passed to the
`deleteArchive()` method of the `opentok` instance. Now that the archive has been deleted, a
redirect response back to the first page of the history is sent back to the browser.

That completes the walkthrough for this Archiving sample application. Feel free to continue to use
this application to browse the archives created for your API Key.
