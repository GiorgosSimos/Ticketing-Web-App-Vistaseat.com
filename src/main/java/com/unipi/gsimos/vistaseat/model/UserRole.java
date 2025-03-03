package com.unipi.gsimos.vistaseat.model;

import lombok.Getter;

@Getter
public enum UserRole {
    REGISTERED,
    DOMAIN_ADMIN, // Top level administrator
    EVENT_ADMIN, // Manages one or more events
    VENUE_ADMIN // Manages one or more venues and its events
}
