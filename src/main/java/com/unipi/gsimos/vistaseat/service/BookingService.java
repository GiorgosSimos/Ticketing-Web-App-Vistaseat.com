package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.BookingInfo;

import java.time.LocalDate;

public interface BookingService {

    BookingInfo prepareBookingInfo(Long occurrenceId, int requestedTickets);

    Long countBookingsByVenueAndDateBetween(Long venueId, LocalDate windowStart, LocalDate windowEnd);
}
