package com.opentok.exception;

/**
* Defines an exception object thrown when an invalid argument is passed into a method.
*/
public class InvalidArgumentException extends OpenTokException {

    private static final long serialVersionUID = 6315168083117996091L;

    /**
     * Constructor. Do not use.
     */
    public InvalidArgumentException(String message) {
        super(message);
    }

}
