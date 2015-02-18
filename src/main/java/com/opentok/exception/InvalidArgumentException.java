/**
 * OpenTok Java SDK
 * Copyright (C) 2015 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
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
