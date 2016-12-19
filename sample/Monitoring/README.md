# OpenTok Session Management & Monitoring Sample for Java

This simple demo app shows how to use the OpenTok Java SDK to do the following:

* Register callbacks to receive notifications for streams and connections created and destroyed
  in an OpenTok session

* Send arbitrary messages to clients in an OpenTok session

* Disconnect participants in OpenTok sessions

## Running the App

First, add your own API Key and API Secret to the system properties. For your convenience, the
`build.gradle` file is set up for you to place your values into it.

```
run.systemProperty 'API_KEY', '000000'
run.systemProperty 'API_SECRET', 'abcdef1234567890abcdef01234567890abcdef'
```

This sample app needs to be reachable from OpenTok servers in order to receive notifications.
Usually, your machine doesn't have a public IP address. There are a number of public services
that can help you overcome that limitation. For example you can use
[localtunnel](https://localtunnel.github.io) or ngrok(https://ngrok.com/).

First, install localtunnel and start the process to listen for HTTP requests on the port where your
server is listening (by default 5000 in this sample app):

```
npm install -g localtunnel
lt --port 5000
```

You will get a public URL from the `lt` command. Add it to the `PUBLIC_URL` system propertie in the `build.gradle` file:

```
run.systemProperty 'PUBLIC_URL', 'http://orfsvirfmv.localtunnel.me'
```

Next, start the server using Gradle (which handles dependencies and setting up the environment).

```
$ gradle :sample/Monitoring:run
```

Or if you are using the Gradle Wrapper that is distributed with the project, from the root project
directory:

```
$ ./gradlew :sample/Archiving:run
```

Visit <http://localhost:4567> in your browser. Open also <http://localhost:4567/events> and start
looking at the events received from OpenTok.


## Walkthrough

This demo application uses the same frameworks and libraries as the HelloWorld sample. If you have
not already gotten familiar with the code in that project, consider doing so before continuing.

The explanations focus on a separate piece of functionality defined by the OpenTok Cloud API.

### Registering callbacks

First, the sample app registers callbacks for all the events defined by the OpenTok Cloud API.
By default, the app uses a single URL to receive all the events, but you can register a different
URL for each event if that simplifies your implementation:

```java
String url = publicUrl + "/callback";
opentok.registerCallback(CallbackGroup.CONNECTION, CallbackEvent.CREATED, url);
opentok.registerCallback(CallbackGroup.CONNECTION, CallbackEvent.DESTROYED, url);
opentok.registerCallback(CallbackGroup.STREAM, CallbackEvent.CREATED, url);
opentok.registerCallback(CallbackGroup.STREAM, CallbackEvent.DESTROYED, url);
```

To register a callback for an OpenTok Cloud event, call the `OpenTok.registerCallback()` method
of the OpenTok Java SDK. The first parameter, `'group'`, defining the group of events you are
interested in, can be set to `'archive'`, `'connection'`, or `'stream'`. The second parameter,
`'type'`, can be set to `'status'` for `'archive'` events, and it can be set to `'created'` or
`'destroyed'` for the connection and stream groups. The third parameter sets the callback URL.
This app sets callback URLs to be called when OpenTok connections or streams are created or
destroyed.

The next step is to define a route to handle the HTTP callback requests coming from OpenTok to your
server. The sample app we use a single route/URL to receive all events, and we store the events
in a list.

```java
    private static List<JsonNode> events = new ArrayList<JsonNode>();
```

```java
    post(new FreeMarkerTemplateView("/callback") {
        @Override
        public Object handle(Request request, Response response) {
            String body = request.body();

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode obj = mapper.readTree(body);
                events.add(obj);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    });
```

The list of events is displayed in the /events test page (<http://localhost:4567/events>). That is
a simple template page showing the events that are stored in the `events` list.

### Sending signals

In the sample app we register a URL (/signal) to send signals to a specific connection in a session.
That URL is called from the /events (<http://localhost:4567/events>) page. The content of the signal
is fixed in the sample app, but you can change it for your specific use case:

```java
    post(new FreeMarkerTemplateView("/signal") {
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
```

To send a signal, call the `OpenTok.signal()` method of the OpenTok Java SDK. The first parameter
is the ID of the OpenTok session, and the second parameter is the connection ID of the client to
receive the signal. The second parameter is optional, and you can call the signal method without it
to send the signal to all clients connected to the session. The third parameter us a signal object
with the `type` and `data` strings to send as the signal payload.

This is the server-side equivalent to the signal() method in the OpenTok client SDKs. See
<https://www.tokbox.com/developer/guides/signaling/js/>.

### Disconnecting participants

The sample app registers a URL (/disconnect) to disconnect a specific connection from a session.
That URL is called from the /events (<http://localhost:4567/events>) page.

```java
    post(new FreeMarkerTemplateView("/disconnect") {
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
```

To disconnect a client from a session, call the `OpenTok.force_disconnect()` method of the OpenTok
Java SDK. The first parameter is the session ID, and the second parameter is the connection ID of
the client to disconnect.

This is the server-side equivalent to the forceDisconnect() method in OpenTok.js:
<https://www.tokbox.com/developer/guides/moderation/js/#force_disconnect>.