# OpenTok Java SDK

[![Build Status](https://travis-ci.org/opentok/Opentok-Java-SDK.svg?branch=master)](https://travis-ci.org/opentok/Opentok-Java-SDK)
[![codecov](https://codecov.io/gh/opentok/Opentok-Java-SDK/branch/master/graph/badge.svg)](https://codecov.io/gh/opentok/Opentok-Java-SDK)

The OpenTok Java SDK lets you generate
[sessions](https://tokbox.com/developer/guides/create-session/) and
[tokens](https://tokbox.com/developer/guides/create-token/) for
[OpenTok](http://www.tokbox.com/) applications that run on the JVM. It also includes methods for
working with OpenTok [archives](https://tokbox.com/developer/guides/archiving),
working with OpenTok [live streaming
broadcasts](https://tokbox.com/developer/guides/broadcast/live-streaming/),
working with OpenTok [SIP interconnect](https://tokbox.com/developer/guides/sip),
[signaling OpenTok sessions from the server](https://tokbox.com/developer/guides/signaling/),
and [disconnecting clients from sessions](https://tokbox.com/developer/guides/moderation/rest/).

## Installation

### Maven Central (recommended):

The [Maven Central](http://central.sonatype.org/) repository helps manage dependencies for JVM
based projects. It can be used via several build tools, including Maven and Gradle.

#### Maven

When you use Maven as your build tool, you can manage dependencies in the `pom.xml` file:

```xml
<dependency>
    <groupId>com.tokbox</groupId>
    <artifactId>opentok-server-sdk</artifactId>
    <version>4.3.0</version>
</dependency>
```

#### Gradle

When you use Gradle as your build tool, you can manage dependencies in the `build.gradle` file:

```groovy
dependencies {
  compile group: 'com.tokbox', name: 'opentok-server-sdk', version: '4.3.0'
}
```

### Manually:

Download the jar file for the latest release from the
[Releases](https://github.com/opentok/opentok-java-sdk/releases) page. Include it in the classpath
for your own project by
[using the JDK directly](http://docs.oracle.com/javase/7/docs/technotes/tools/windows/classpath.html)
or in your IDE of choice.

## Usage

### Initializing

Import the required classes in any class where it will be used. Then initialize a `com.opentok.OpenTok`
object with your own API Key and API Secret.

```java
import com.opentok.OpenTok;

// inside a class or method...
int apiKey = 000000; // YOUR API KEY
String apiSecret = "YOUR API SECRET";
OpenTok opentok = new OpenTok(apiKey, apiSecret)
```

And make sure you call `close` when you are done to prevent leaked file descriptors.

```java
opentok.close();
```

### Creating Sessions

To create an OpenTok Session, use the `OpenTok` instance’s `createSession(SessionProperties properties)`
method. The `properties` parameter is optional and it is used to specify two things:

* Whether the session uses the OpenTok Media Router
* A location hint for the OpenTok server.
* Whether the session is automatically archived.

An instance can be initialized using the `com.opentok.SessionProperties.Builder` class.
The `sessionId` property of the returned `com.opentok.Session` instance, which you can read using
the `getSessionId()` method, is useful to get an identifier that can be saved to a persistent store
(such as a database).

```java
import com.opentok.MediaMode;
import com.opentok.ArchiveMode;
import com.opentok.Session;
import com.opentok.SessionProperties;

// A session that attempts to stream media directly between clients:
Session session = opentok.createSession();

// A session that uses the OpenTok Media Router:
Session session = opentok.createSession(new SessionProperties.Builder()
  .mediaMode(MediaMode.ROUTED)
  .build());

// A Session with a location hint:
Session session = opentok.createSession(new SessionProperties.Builder()
  .location("12.34.56.78")
  .build());

// A session that is automatically archived (it must used the routed media mode)
Session session = opentok.createSession(new SessionProperties.Builder()
  .mediaMode(MediaMode.ROUTED)
  .archiveMode(ArchiveMode.ALWAYS)
  .build());

// Store this sessionId in the database for later use:
String sessionId = session.getSessionId();
```

### Generating Tokens

Once a Session is created, you can start generating Tokens for clients to use when connecting to it.
You can generate a token either by calling an `com.opentok.OpenTok` instance's
`generateToken(String sessionId, TokenOptions options)` method, or by calling a `com.opentok.Session`
instance's `generateToken(TokenOptions options)` method after creating it. The `options` parameter
is optional and it is used to set the role, expire time, and connection data of the token. An
instance can be initialized using the `TokenOptions.Builder` class.

```java
import com.opentok.TokenOptions;
import com.opentok.Role;

// Generate a token from just a sessionId (fetched from a database)
String token = opentok.generateToken(sessionId);
// Generate a token by calling the method on the Session (returned from createSession)
String token = session.generateToken();

// Set some options in a token
String token = session.generateToken(new TokenOptions.Builder()
  .role(Role.MODERATOR)
  .expireTime((System.currentTimeMillis() / 1000L) + (7 * 24 * 60 * 60)) // in one week
  .data("name=Johnny")
  .build());
```

### Working with Archives

You can only archive sessions that use the OpenTok Media Router
(sessions with the media mode set to routed).

You can start the recording of an OpenTok Session using a `com.opentok.OpenTok` instance's
`startArchive(String sessionId, String name)` method. This will return a `com.opentok.Archive` instance.
The parameter `name` is optional and used to assign a name for the Archive. Note that you can
only start an Archive on a Session that has clients connected.

```java
import com.opentok.Archive;

// A simple Archive (without a name)
Archive archive = opentok.startArchive(sessionId, null);

// Store this archiveId in the database for later use
String archiveId = archive.getId();
```

You can also disable audio or video recording by calling the `hasAudio(false)` or `hasVideo(false)`
methods of an `ArchiveProperties` builder, and passing the built object into the
`OpenTok.startArchive(String sessionId, ArchiveProperties properties)` method:

```java
import com.opentok.Archive;
import com.opentok.ArchiveProperties;

// Start an audio-only archive
Archive archive = opentok.startArchive(sessionId, new ArchiveProperties.Builder()
  .hasVideo(false)
  .build()););

// Store this archiveId in the database for later use
String archiveId = archive.getId();
```

Setting the output mode to `Archive.OutputMode.INDIVIDUAL` setting causes each stream in the archive
to be recorded to its own individual file:

```java
import com.opentok.Archive;
import com.opentok.ArchiveProperties;

Archive archive = opentok.startArchive(sessionId, new ArchiveProperties.Builder()
  .archiveMode(Archive.OutputMode.INDIVIDUAL)
  .build()););

// Store this archiveId in the database for later use
String archiveId = archive.getId();
```

The `Archive.OutputMode.COMPOSED` setting (the default) causes all streams in the archive to be
recorded to a single (composed) file.

You can set the composed archive resolution to either "640x480" (SD, the default) or "1280x720" (HD) using the ArchiveProperties builder.
Any other value will result in an exception.The property only applies to composed archives. 
If you set this property and also set the outputMode property to "individual", the method results in an `InvalidArgumentException`.

```java
import com.opentok.ArchiveProperties;

ArchiveProperties properties = new ArchiveProperties.Builder().resolution("1280x720").build();
```

You can stop the recording of a started Archive using a `com.opentok.Archive` instance's
`stopArchive(String archiveId)` method.

```java
// Stop an Archive from an archiveId (fetched from database)
Archive archive = opentok.stopArchive(archiveId);
```

To get an `com.opentok.Archive` instance (and all the information about it) from an `archiveId`, use
a `com.opentok.OpenTok` instance's `getArchive(String archiveId)` method.

```java
Archive archive = opentok.getArchive(String archiveId);
```

To delete an Archive, you can call a `com.opentok.OpenTok` instance's `deleteArchive(String archiveId)`
method.

```java
// Delete an Archive from an archiveId (fetched from database)
opentok.deleteArchive(archiveId);
```

You can also get a list of all the Archives you've created (up to 1000) with your API Key. This is
done using a `com.opentok.OpenTok` instance's `listArchives(int offset, int count)` method. You may optionally
paginate the Archives you receive using the offset and count parameters. This will return a
`List<Archive>` type. An `InvalidArgumentException` will be thrown if the offset or count are
negative or if the count is greater than 1000.

```java
// Get a list with the first 1000 archives created by the API Key
List<Archive> archives = opentok.listArchives();

// Get a list of the first 50 archives created by the API Key
List<Archive> archives = opentok.listArchives(0, 50);

// Get a list of the next 50 archives
List<Archive> archives = opentok.listArchives(50, 50);
```
You can also fetch the list of archives for a specific session ID , and optionally 
use the offset and count parameters as described above.

```java
// Get a list with the first 1000 archives for a specific session)
ArchiveList archives = opentok.listArchives(sessionId);

// Get a list of the first 50 archives  for a specific session
ArchiveList archives = sdk.listArchives(sessionId, 0, 50);

// Get a list of the next 50 archives for a specific session
ArchiveList archives = sdk.listArchives(sessionId, 50, 50);
```

Note that you can also create an automatically archived session, by passing `ArchiveMode.ALWAYS`
into the `archiveMode()` method of the `SessionProperties.Builder` object you use to build the
`sessionProperties` parameter passed into the `OpenTok.createSession()` method (see "Creating
Sessions," above).

For composed archives, you can dynamically set the archive layout (while the archive is being recorded) using the `OpenTok.setArchiveLayout(String archiveId, ArchiveProperties properties)` 
method. See [Customizing the video layout for composed
archives](https://tokbox.com/developer/guides/archiving/layout-control.html) for more information. Use the `ArchiveProperties` builder as follows:

```java
ArchiveProperties properties = new ArchiveProperties.Builder()
    .layout(new ArchiveLayout(ArchiveLayout.Type.VERTICAL))
    .build();
opentok.setArchiveLayout(archiveId, properties);
```

For custom layouts the builder looks like:

```java
ArchiveProperties properties = new ArchiveProperties.Builder()
.layout(new ArchiveLayout(ArchiveLayout.Type.CUSTOM, "stream { position: absolute; }"))
.build();
```

You can set the initial layout class for a client's streams by setting the `layout`
option when you create the token for the client, using the
`OpenTok.generateToken(String sessionId, TokenOptions options)` method. And you can
also change the layout classes of a stream as follows:

```java
StreamProperties streamProps = new StreamProperties.Builder()
  .id(streamId)
  .addLayoutClass("full")
  .addLayoutClass("focus")
  .build();
StreamListProperties properties = new StreamListProperties.Builder()
  .addStreamProperties(streamProps)
  .build();
opentok.setStreamLayouts(sessionId, properties);
```

If you want to change the layout of multiple streams, create a StreamProperties object
for each stream, and add them to the StreamListProperties object as follows:

```java
StreamListProperties properties = new StreamListProperties.Builder()
  .addStreamProperties(streamProps1)
  .addStreamProperties(streamProps2)
  .build();
opentok.setStreamLayouts(sessionId, properties);
```

For more information on archiving, see the
[OpenTok archiving](https://tokbox.com/developer/guides/archiving/) developer guide.

### Disconnecting Clients

Your application server can disconnect a client from an OpenTok session by calling the `forceDisconnect(sessionId, connectionId)`
method of the `com.opentok.OpenTok` instance. 

```java
opentok.forceDisconnect(sessionId, connectionId);
```

The `connectionId` parameter is used to specify the connection ID of a client connection to the session.

For more information on the force disconnect functionality and exception codes, please see the [REST API documentation](https://tokbox.com/developer/rest/#forceDisconnect).

### Signaling

You can send signals to all the connections in a session or to a specific connection:

- `public void signal(String sessionId, SignalProperties props) throws OpenTokException , RequestException, InvalidArgumentException `

- `public void signal(String sessionId, String connectionId, SignalProperties props) throws OpenTokException , RequestException , InvalidArgumentException`

The `SignalProperties` builder helps you to construct the signal data and type:


```java
SignalProperties properties = new SignalProperties.Builder()
  .type("test")
  .data("This is a test string")
  .build();
 
opentok.signal(sessionId, properties);
opentok.signal(sessionId, connectionId, properties);
```

Make sure that the `type` string does not exceed the maximum length (128 bytes)
and the `data` string does not exceed the maximum length (8 kB). 
The `SignalProperties` builder does not currently check for these limitations.

For more information on signaling and exception codes, refer to the documentation for the
[OpenTok signaling](https://tokbox.com/developer/rest/#send_signal) REST method.

### Broadcasting

You can broadcast OpenTok publishing streams to an HLS (HTTP live streaming) or 
to RTMP streams. To successfully start broadcasting a session, at least one client must be 
connected to the session. You can only have one active live streaming broadcast at a time
for a session (however, having more than one would not be useful).
The live streaming broadcast can target one HLS endpoint and up to five 
RTMP servers simulteneously for a session. You can only start live streaming
for sessions that use the OpenTok Media Router (with the media mode set to routed);
you cannot use live streaming with sessions that have the media mode set to relayed. 
(See the [OpenTok Media Router and media
modes](https://tokbox.com/developer/guides/create-session/#media-mode) developer guide).
 
You can start a broadcast using the `OpenTok.startBroadcast(sessionId, properties)` method,
where the `properties` field is a `BroadcastProperties` object. Initalize a `BroadcastProperties`
object as follows (see the [Opentok Broadcast](https://tokbox.com/developer/rest/#start_broadcast)
REST method for more details):

```java
BroadcastProperties properties = new BroadcastProperties.Builder()
        .hasHls(true)
        .addRtmpProperties(rtmpProps)
        .addRtmpProperties(rtmpNextProps)
        .maxDuration(1000)
        .resolution("640x480")
        .layout(layout)
        .build();

// The Rtmp properties can be build using RtmpProperties as shown below
RtmpProperties rtmpProps = new RtmpProperties.Builder()
        .id("foo")
        .serverUrl("rtmp://myfooserver/myfooapp")
        .streamName("myfoostream").build();

//The layout object is initialized as follows:
BroadcastLayout layout = new BroadcastLayout(BroadcastLayout.Type.PIP);
```

Finally, start a broadcast as shown below:

```java
Broadcast broadcast = opentok.startBroadcast(sessionId, properties)
```

The `Broadcast` object returned has the following info:

```java
String broadcastId;
String sessionId;
int projectId;
long createdAt;
long updatedAt;
String resolution;
String status;
List<Rtmp> rtmpList = new ArrayList<>();  //not more than 5 
String hls;    // HLS url

// The Rtmp class mimics the RtmpProperties
```

To stop a broadcast use:  

```java
Broadcast broadcast = opentok.stopBroadcast(broadcastId);
```

To get more information about a live streaming broadcast, use:

```java
Broadcast broadcast = opentok.getBroadcast(broadcastId);
```

The information returned is in the `Broadcast` object and consists of HLS and/or Rtmp URLs,
along with the session ID, resolution, etc.

You can also change the
[layout](https://tokbox.com/developer/guides/broadcast/live-streaming/#configuring-video-layout-for-opentok-live-streaming-broadcasts)
of a live broadcast dynamically using:

```java
opentok.setBroadcastLayout(broadcastId, properties);

//properties can be 
BroadcastProperties properties = new BroadcastProperties.Builder()
          .layout(new BroadcastLayout(BroadcastLayout.Type.VERTICAL))
          .build();
```

To dynamically change the layout class of an individual stream, use

```java
StreamProperties streamProps = new StreamProperties.Builder()
          .id(streamId)
          .addLayoutClass("full")
          .addLayoutClass("focus")
          .build();
StreamListProperties properties = new StreamListProperties.Builder()
          .addStreamProperties(streamProps)
          .build();
opentok.setStreamLayouts(sessionId, properties);
```

### Working with Streams

You can get information about a stream by calling the `getStream(sessionId, streamId)` method
of the `com.opentok.OpenTok` instance. 

```java
// Get stream info from just a sessionId (fetched from a database)
Stream stream = opentok.getStream(sessionId, streamId);

// Stream Properties
stream.getId(); // string with the stream ID
stream.getVideoType(); // string with the video type
stream.getName(); // string with the name
stream.layoutClassList(); // List with the layout class list
```

You can get information about all of the streams in a session by calling the `listStreams(sessionId)` method of the `com.opentok.OpenTok` instance.

```java

// Get list of strems from just a sessionId (fetched from a database)
StreamList streamList = opentok.listStreams(sessionId);

streamList.getTotalCount(); // total count
```

### Working with SIP Interconnect

You can add an audio-only stream from an external third party SIP gateway using the SIP
Interconnect feature. This requires a SIP URI, the session ID you wish to add the audio-only
stream to, and a token to connect to that session ID.

To connect your SIP platform to an OpenTok session, call the
`OpenTok.dial(String sessionId, String token, SipProperties properties)` method. 
The audio from your end of the SIP call is added to the OpenTok session as an audio-only stream. 
The OpenTok Media Router mixes audio from other streams in the session and sends the mixed audio
to your SIP endpoint. The call ends when your SIP server sends a BYE message (to terminate
the call). You can also end a call using the `OpenTok.forceDisconnect(sessionId, connectionId)`
method to disconnect the SIP client from the session (see [Disconnecting clients](#disconnecting-clients)).

The OpenTok SIP gateway automatically ends a call after 5 minutes of inactivity
(5 minutes without media received). Also, as a security measure, 
the OpenTok SIP gateway closes any SIP call that lasts longer than 6 hours.

The SIP interconnect feature requires that you use an OpenTok session that uses the 
OpenTok Media Router (a session with the media mode set to routed).

To connect an OpenTok session to a SIP gateway:

```java
SipProperties properties = new SipProperties.Builder()
         .sipUri("sip:user@sip.partner.com;transport=tls")
         .from("from@example.com")
         .headersJsonStartingWithXDash(headerJson)
         .userName("username")
         .password("password")
         .secure(true)
         .build();

 Sip sip = opentok.dial(sessionId, token, properties);
```

## Samples

There are two sample applications included with the SDK. To get going as fast as possible, clone the whole
repository and follow the Walkthroughs:

*  [HelloWorld](sample/HelloWorld/README.md)
*  [Archiving](sample/Archiving/README.md)

## Documentation

Reference documentation is available at
<https://tokbox.com/developer/sdks/java/reference/index.html>.

## Requirements

You need an OpenTok API key and API secret, which you can obtain by logging into your
[TokBox account](https://tokbox.com/account).

The OpenTok Java SDK requires JDK 8 or greater to compile. Runtime requires Java SE 8 or greater.
This project is tested on both OpenJDK and Oracle implementations.

For Java 7 please use OpenTok Java SDK v3.

## Release Notes

See the [Releases](https://github.com/opentok/opentok-java-sdk/releases) page for details
about each release.

## Important changes since v2.2.0

**Changes in v2.2.1:**

The default setting for the `createSession()` method is to create a session with the media mode set
to relayed. In previous versions of the SDK, the default setting was to use the OpenTok Media Router
(with the media mode set to routed in v2.2.0, or with p2p.preference="disabled" in previous
versions). In a relayed session, clients will attempt to send streams directly between each other
(peer to peer); and if clients cannot connect due to firewall restrictions, the session uses the
OpenTok TURN server to relay audio-video streams.

**Changes in v2.2.0:**

This version of the SDK includes support for working with OpenTok archives.

This version of the SDK includes a number of improvements in the API design. These include a number
of API changes. See the OpenTok 2.2 SDK Reference for details on the new API.

The API_Config class has been removed. Store your OpenTok API key and API secret in code outside of the SDK files.

The `create_session()` method has been renamed `createSession()`. Also, the method has changed to
take one parameter: a SessionProperties object. You now generate a SessionProperties object using a Builder pattern.

The `generate_token()` method has been renamed `generateToken()`. Also, the method has changed to
take two parameters: the session ID and a TokenOptions object.

## Development and Contributing

Interested in contributing? We :heart: pull requests! See the [Development](DEVELOPING.md) and
[Contribution](CONTRIBUTING.md) guidelines.

## Support

See <https://support.tokbox.com> for all our support options.

Find a bug? File it on the [Issues](https://github.com/opentok/opentok-java-sdk/issues) page. Hint:
test cases are really helpful!
