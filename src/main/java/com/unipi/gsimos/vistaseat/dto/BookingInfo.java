package com.unipi.gsimos.vistaseat.dto;

import java.math.BigDecimal;

// Helper record to store all the necessary info for the make Booking page
public record BookingInfo(
        int numberOfTickets,
        BigDecimal serviceFee,
        BigDecimal totalAmount,
        EventOccurrenceCardDto occurrenceCard,
        EventCardDto eventCard,
        Long venueId) {
}
