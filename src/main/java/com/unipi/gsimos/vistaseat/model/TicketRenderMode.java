package com.unipi.gsimos.vistaseat.model;

public enum TicketRenderMode {
    VIEW,
    DOWNLOAD;

    public String getContentDispositionValue() {
        return this == VIEW ? "inline" : "attachment";
    }
}
