/**
 * OpenTok Java SDK
 * Copyright (C) 2016 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

/**
 * Defines the fields of a signal to be sent to an OpenTok session or connection.
 *
 * @see OpenTok#signal()
*/
public class Signal {
    private String type;
    private String data;

    /**
     * The type field of the signal.
     */
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    /**
     * The data field of the signal.
     */
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
}
