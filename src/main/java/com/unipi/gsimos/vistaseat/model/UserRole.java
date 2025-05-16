package com.unipi.gsimos.vistaseat.model;

import lombok.Getter;

@Getter
public enum UserRole {
    REGISTERED, // Simple user
    DOMAIN_ADMIN, // Top level administrator, manages all venues and events
    EVENT_ADMIN, // Manages one or more events
    VENUE_ADMIN // Manages one or more venues and its events
}
