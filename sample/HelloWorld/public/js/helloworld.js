// Initialize an OpenTok Session object
var session = OT.initSession(apiKey, sessionId);

// Initialize a Publisher, and place it into the element with id="publisher"
var publisher = OT.initPublisher('publisher');

// Attach an event handler for when the session dispatches the 'streamCreated' event.
session.on('streamCreated', function(event) {
  // This function runs when another client publishes a stream (eg. session.publish())

  // Subscribe to the stream that caused this event, put it inside the DOM element with id='subscribers'
  session.subscribe(event.stream, 'subscribers', {
    insertMode: 'append'
  }, function(error) {
    if (error) {
      console.error('Failed to subscribe', error);
    }
  });
});

// Connect to the Session using the 'apiKey' of the application and a 'token' for permission
session.connect(token, function(error) {
  // This function runs when session.connect() asynchronously completes

  // Handle connection errors
  if (error) {
    console.error('Failed to connect', error);
  } else {
    // Publish the publisher we initialzed earlier (this will trigger 'streamCreated' on other
    // clients)
    session.publish(publisher, function(error) {
      if (error) {
        console.error('Failed to publish', error);
      }
    });
  }
});
