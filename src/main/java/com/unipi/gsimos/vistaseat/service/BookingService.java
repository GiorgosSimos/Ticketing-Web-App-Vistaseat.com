package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.BookingInfo;
import com.unipi.gsimos.vistaseat.dto.PendingBookingDto;
import com.unipi.gsimos.vistaseat.model.PaymentMethods;

import java.time.LocalDate;

public interface BookingService {

    Long createBooking(PendingBookingDto pendingBookingDto);

    BookingInfo prepareBookingInfo(Long occurrenceId, int requestedTickets);

    void confirmBooking(Long bookingId, PaymentMethods paymentMethod);

    Long countBookingsByVenueAndDateBetween(Long venueId, LocalDate windowStart, LocalDate windowEnd);


}
