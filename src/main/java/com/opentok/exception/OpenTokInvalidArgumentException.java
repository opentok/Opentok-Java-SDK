package com.opentok.exception;

public class OpenTokInvalidArgumentException extends OpenTokException {

    private static final long serialVersionUID = 6315168083117996091L;

    public OpenTokInvalidArgumentException(String statusMessage) {
        super(400, statusMessage);
    }
}
