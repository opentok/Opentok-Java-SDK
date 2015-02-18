/**
 * OpenTok Java SDK
 * Copyright (C) 2015 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.exception;

public class InvalidArgumentException extends OpenTokException {

    private static final long serialVersionUID = 6315168083117996091L;

    public InvalidArgumentException(String message) {
        super(message);
    }

}
