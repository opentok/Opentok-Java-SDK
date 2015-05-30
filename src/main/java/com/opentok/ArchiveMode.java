/**
 * OpenTok Java SDK
 * Copyright (C) 2015 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

/**
 * Defines values for the archiveMode parameter of the
 * {@link SessionProperties.Builder#archiveMode(ArchiveMode archiveMode)} method.
 */
public enum ArchiveMode {

    /**
     * The session is not archived automatically. To archive the session, you can call the
     * OpenTok.StartArchive() method.
     */
    MANUAL ("manual"),

    /**
     * The session is archived automatically (as soon as there are clients publishing streams
     * to the session).
     */
    ALWAYS ("always");

    private String serialized;

    private ArchiveMode(String s) {
        serialized = s;
    }

    @Override
    public String toString() {
        return serialized;
    }
}
