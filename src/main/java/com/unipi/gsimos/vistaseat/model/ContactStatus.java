package com.unipi.gsimos.vistaseat.model;

public enum ContactStatus {

    IN_PROGRESS,
    RESOLVED;

    public String displayStatus() {
        return switch (this) {
            case IN_PROGRESS ->  "In Progress";
            case RESOLVED ->  "Resolved";
        };
    }

}
