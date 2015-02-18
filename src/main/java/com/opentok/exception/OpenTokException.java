/**
 * OpenTok Java SDK
 * Copyright (C) 2015 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok.exception;

/**
* Defines exceptions in the OpenTok SDK.
*/
public class OpenTokException extends Exception {
	private static final long serialVersionUID = 6059658348908505724L;

    /**
     * Constructor. Do not use.
     */
    public OpenTokException(String message) {
        super(message);
    }

    /**
     * Constructor. Do not use.
     */
    public OpenTokException(String message, Throwable cause) {
        super(message, cause);
    }
}