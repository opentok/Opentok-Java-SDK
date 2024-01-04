/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
 * http://www.tokbox.com
 * <p>
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
    MANUAL,

    /**
     * The session is archived automatically (as soon as there are clients connected
     * to the session).
     */
    ALWAYS;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
