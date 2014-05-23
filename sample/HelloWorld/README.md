# OpenTok Hello World Java

This is a simple demo app that shows how you can use the OpenTok Java SDK to create Sessions,
generate Tokens with those Sessions, and then pass these values to a JavaScript client that can
connect and conduct a group chat.

## Running the App

First, add your own API Key and API Secret to the system properties. For your convenience, the
`build.gradle` file is set up for you to place your values into it.

```
run.systemProperty 'API_KEY', '000000'
run.systemProperty 'API_SECRET', 'abcdef1234567890abcdef01234567890abcdef'
```

Next, start the server using Gradle (which handles dependencies and setting up the environment).

```
$ gradle :sample/HelloWorld:run
```

Or if you are using the Gradle Wrapper that is distributed with the project, from the root project
directory:

```
$ ./gradlew :sample/HelloWorld:run
```

Visit <http://localhost:4567> in your browser. Open it again in a second window. Smile! You've just
set up a group chat.

## Walkthrough

This demo application uses the [Spark micro web framework](http://www.sparkjava.com/). It is similar to
many other popular web frameworks. We are only covering the very basics of the framework, but you can
learn more by following the link above.

### Main Application (src/main/java/com/example/HelloWorldServer.java)

The first thing done in this file is to import the dependencies we will be using. In this case that
is the Spark web framework, a couple collection classes, and most importantly some classes from the
OpenTok SDK.

```java
import static spark.Spark.*;
import spark.*;

import java.util.Map;
import java.util.HashMap;

import com.opentok.OpenTok;
import com.opentok.exception.OpenTokException;
```

Next, we set up a main class for the application.

```java
public class HelloWorldServer {

  // We will set up some class variables here

  public static void main(String[] args) throws OpenTokException {
    // The application will start here
  }
}
```

Next this application performs some basic checks on the environment. If it cannot find the `API_KEY`and
`API_SECRET` system properties, there is no point in continuing.


```java
public class HelloWorldServer {

  private static final String apiKey = System.getProperty("API_KEY");
  private static final String apiSecret = System.getProperty("API_SECRET");

  public static void main(String[] args) throws OpenTokException {

    if (apiKey == null || apiKey.isEmpty() || apiSecret == null || apiSecret.isEmpty()) {
      System.out.println("You must define API_KEY and API_SECRET system properties in the build.gradle file.");
      System.exit(-1);
    }

  }
}
```

The first thing the application does is to initialize an instance of `OpenTok` and store it as
a static class variable.

```java
public class HelloWorldServer {

  // ...
  private static OpenTok opentok;

  public static void main(String[] args) throws OpenTokException {
    // ...
    opentok = new OpenTok(Integer.parseInt(apiKey), apiSecret);
  }
}
```

Now, lets discuss the Hello World application's functionality. We want to set up a group chat so
that any client that visits a page will connect to the same OpenTok Session. Once they are connected
they can Publish a Stream and Subscribe to all the other streams in that Session. So we just need
one Session object, and it needs to be accessible every time a request is made. The next line of our
application simply calls the `OpenTok` instance's `createSession()` method and pulls out the
`String sessionId` using the `getSessionId()` method on the resulting `Session` instance. This is
stored in another class variable. Alternatively, `sessionId`s are commonly stored in databses for
applications that have many of them.

```java
public class HelloWorldServer {

  // ...
  private static String sessionId;

  public static void main(String[] args) throws OpenTokException {
    // ...

    sessionId = opentok.createSession().getSessionId();
  }
}
```

Spark uses the `externalStaticFileLocation()` method to specify which directory to serve static
files from.

```java
public class HelloWorldServer {

  // ...

  public static void main(String[] args) throws OpenTokException {
    // ...

    externalStaticFileLocation("./public");
  }
}
```

We only need one page, so we create one route handler for any HTTP GET requests to trigger.

```java
public class HelloWorldServer {

  // ...

  public static void main(String[] args) throws OpenTokException {
    // ...

    get(new FreeMarkerTemplateView("/") {
      @Override
      public ModelAndView handle(Request request, Response response) {

      // This is where we handle the request and are responsible for returning a response

      }
    });

  }
}

```

Now all we have to do is serve a page with the three values the client will need to connect to the
session: `apiKey`, `sessionId`, and `token`. The first two are available as class variables. The
`token` is generated freshly on this request by calling `opentok.generateToken()`, and passing in
the `sessionId`. This is because a Token is a piece of information that carries a specific client's
permissions in a certain Session. Ideally, as we've done here, you generate a unique token for each
client that will connect.

```java
    get(new FreeMarkerTemplateView("/") {
      @Override
      public ModelAndView handle(Request request, Response response) {

        String token = null;
        try {
            token = opentok.generateToken(sessionId);
        } catch (OpenTokException e) {
            e.printStackTrace();
        }

        // Now we have apiKey, sessionId, and token

      }
    });
```

Now all we have to do is serve a page with those three values. To do so, we put together a Map of
values that our template system (freemarker) will use to render an HTML page. This is done by
returning an instance of `ModelAndView` that groups this map with the name of a view.

```java
    get(new FreeMarkerTemplateView("/") {
      @Override
      public ModelAndView handle(Request request, Response response) {
        // ...

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("apiKey", apiKey);
        attributes.put("sessionId", sessionId);
        attributes.put("token", token);

        return new ModelAndView(attributes, "index.ftl");
      }
    });
```

### Main Template (src/main/resources/com/example/freemarker/index.ftl)

This file simply sets up the HTML page for the JavaScript application to run, imports the OpenTok.js
JavaScript library, and passes the values created by the server into the JavaScript application
inside `public/js/helloworld.js`

### JavaScript Applicaton (public/js/helloworld.js)

The group chat is mostly implemented in this file. At a high level, we connect to the given
Session, publish a stream from our webcam, and listen for new streams from other clients to
subscribe to.

For more details, read the comments in the file or go to the
[JavaScript Client Library](http://tokbox.com/opentok/libraries/client/js/) for a full reference.
