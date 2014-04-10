package com.opentok.exception;

/**
* Defines exceptions in the OpenTok SDK.
*/
public abstract class OpenTokException extends Exception {
	private static final long serialVersionUID = 6059658348908505724L;

	int errorCode;
    String statusMessage;

    public OpenTokException(int code, String statusMessage) {
        super();
        this.errorCode = code;
        this.statusMessage = statusMessage;
    }

    /**
     * Returns the status message, providing details about the error.
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Returns the error code.
     */
    public int getErrorCode() {
        return errorCode;
    }
}