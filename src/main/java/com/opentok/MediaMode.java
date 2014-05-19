package com.opentok;

public enum MediaMode {
    ROUTED ("disabled"),
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
