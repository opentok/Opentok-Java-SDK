/*!
 * OpenTok Java Library
 * http://www.tokbox.com/
 *
 * Copyright 2010, TokBox, Inc.
 */

package com.opentok;
/**
 * Defines values for the role parameter of the {@link TokenOptions.Builder#role(Role role)} method.
 */
public enum Role {
    /**
    *   A subscriber can only subscribe to streams.
    */
    SUBSCRIBER,
    /**
    * A publisher can publish streams, subscribe to streams, and signal. (This is the default
    * value if you do not set a roll by calling the {@link TokenOptions.Builder#role(Role role)}
    * method.
    */
    PUBLISHER,
    /**
    * In addition to the privileges granted to a publisher, in clients using the OpenTok.js 2.2
    * library, a moderator can call the <code>forceUnpublish()</code> and
    * <code>forceDisconnect()</code> method of the Session object.
    */
    MODERATOR;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
