package com.opentok.exception;

/**
 * Defines an exception object thrown when an API call to the OpenTok server fails.
 */
public class RequestException extends OpenTokException {

    private static final long serialVersionUID = -3852834447530956514L;

    /**
     * Constructor. Do not use.
     */
    public RequestException(String message) {
        super(message);
    }

    /**
     * Constructor. Do not use.
     */
    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
