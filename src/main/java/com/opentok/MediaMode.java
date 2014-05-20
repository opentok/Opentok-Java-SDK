package com.opentok;

/**
 * Defines values for the mediaMode parameter of the
 * {@link SessionProperties.Builder#mediaMode(MediaMode mediaMode)} method.
 */
public enum MediaMode {
    /**
     * The session will transmit streams using the OpenTok Media Server.
     */
    ROUTED ("disabled"),
    /**
     * The session will attempt to transmit streams directly between clients. If two clients
     * cannot send and receive each others' streams, due to firewalls on the clients' networks,
     * their streams will be relayed using the OpenTok TURN Server.
     */
    RELAYED ("enabled");

    private String serialized;

    private MediaMode(String s) {
        serialized = s;
    }

    @Override
    public String toString() {
        return serialized;
    }
}
