/**
 * OpenTok Java SDK
 * Copyright (C) 2018 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

/**
 *  Represents a <a href="https://tokbox.com/developer/guides/broadcast/live-streaming/#configuring-video-layout-for-opentok-live-streaming-broadcasts">layout
 *  configuration</a> for a live streaming broadcast.
 */
public class BroadcastLayout extends ArchiveLayout {
    /**
     * Do not call the <code>BroadcastLayout()</code> constructor. To set the layout of
     * a live streaming broadcast, call the {@link OpenTok#setBroadcastLayout(String broadcastId, BroadcastProperties properties)} method.
     */
    public BroadcastLayout(Type type, String stylesheet) {
        super(type, stylesheet);
    }

    /**
     * Do not call the <code>BroadcastLayout()</code> constructor. To set the layout of
     * a live streaming broadcast, call the {@link OpenTok#setBroadcastLayout(String broadcastId, BroadcastProperties properties)} method.
     */
    public BroadcastLayout(Type type) {
        super(type);
    }
}
