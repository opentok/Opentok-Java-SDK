# OpenTokSDK Java SDK

The OpenTok Server SDK for Java defines an OpenTokSDK class, which needs to be instantiated as an OpenTokSDK object before calling any of its methods.

To create a new OpenTokSDK object, call the OpenTokSDK constructor with the API key 
and the API secret from <a href="https://dashboard.tokbox.com/users/sign_in">your OpenTok dashboard</a>. Do not publicly share 
your API secret. You will use it with the OpenTokSDK constructor (only on your web
server) to create OpenTok sessions.

For details on the API, see the comments in Java files in src/main/java/com/opentok.