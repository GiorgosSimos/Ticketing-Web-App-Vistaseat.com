package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.BookingInfo;
import com.unipi.gsimos.vistaseat.dto.PendingBookingDto;

import java.time.LocalDate;

public interface BookingService {

    Long createBooking(PendingBookingDto pendingBookingDto);

    BookingInfo prepareBookingInfo(Long occurrenceId, int requestedTickets);

    void confirmBooking(Long bookingId);

    void cancelExpiredBookings();

    Long countBookingsByVenueAndDateBetween(Long venueId, LocalDate windowStart, LocalDate windowEnd);


}
