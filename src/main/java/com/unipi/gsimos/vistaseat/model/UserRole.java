package com.unipi.gsimos.vistaseat.model;

import lombok.Getter;

@Getter
public enum UserRole {
    REGISTERED, // Simple user
    DOMAIN_ADMIN, // Top level administrator, manages all venues and events
    EVENT_ADMIN, // Manages one or more events
    VENUE_ADMIN; // Manages one or more venues and its events
    
    public String displayRole() {
        return switch (this) {
            case REGISTERED -> "Registered User";
            case DOMAIN_ADMIN -> "Domain Admin";
            case EVENT_ADMIN -> "Event Admin";
            case VENUE_ADMIN -> "Venue Admin";
        };
    }
}
