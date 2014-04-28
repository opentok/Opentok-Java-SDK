package com.opentok.exception;

public class RequestException extends OpenTokException {

    private static final long serialVersionUID = -3852834447530956514L;

    public RequestException(String message) {
        super(message);
    }

    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
