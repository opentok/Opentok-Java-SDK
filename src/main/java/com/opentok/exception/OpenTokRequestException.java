package com.opentok.exception;

public class OpenTokRequestException extends OpenTokException {

    private static final long serialVersionUID = -3852834447530956514L;

    public OpenTokRequestException(int code, String statusMessage) {
        super(code, statusMessage);

    }

}
