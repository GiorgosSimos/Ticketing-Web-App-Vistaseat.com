package com.unipi.gsimos.vistaseat.model;

import lombok.Getter;

@Getter
public enum TicketStatus {
    ACTIVE, // Ticket is ready to be used
    EXPIRED, // Ticket is already checked or event datetime has passed
    CANCELLED // Ticket is inactive due to event cancellation or refund
}
