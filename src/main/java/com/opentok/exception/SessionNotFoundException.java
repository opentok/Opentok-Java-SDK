package com.opentok.exception;

public class SessionNotFoundException extends OpenTokException {

    private static final long serialVersionUID = 1946440047275860231L;

    public SessionNotFoundException(String message) {
        super(message);
    }
}
