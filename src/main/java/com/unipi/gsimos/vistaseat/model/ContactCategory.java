package com.unipi.gsimos.vistaseat.model;

public enum ContactCategory {

    TICKET_ISSUE,
    REFUND_RESCHEDULE,
    ACCOUNT_LOGIN,
    PARTNERSHIP,
    OTHER;

    public String displayCategory() {
        return switch (this) {
            case TICKET_ISSUE -> "Ticket Issue";
            case REFUND_RESCHEDULE -> "Refund-Reschedule";
            case ACCOUNT_LOGIN -> "Account Login";
            case PARTNERSHIP -> "Partnership";
            case OTHER -> "Other";
        };
    }
}
