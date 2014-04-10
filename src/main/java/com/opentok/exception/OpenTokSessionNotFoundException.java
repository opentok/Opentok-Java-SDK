package com.opentok.exception;

public class OpenTokSessionNotFoundException extends OpenTokException {

    private static final long serialVersionUID = 1946440047275860231L;

    public OpenTokSessionNotFoundException(String statusMessage) {
        super(404, statusMessage);
    }

}
