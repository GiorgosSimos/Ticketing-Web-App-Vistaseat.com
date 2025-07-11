package com.unipi.gsimos.vistaseat.service;

import java.time.LocalDate;

public interface BookingService {

    Long countBookingsByVenueAndDateBetween(Long venueId, LocalDate windowStart, LocalDate windowEnd);
}
