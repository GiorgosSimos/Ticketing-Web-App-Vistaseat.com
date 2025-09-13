package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.BookingDto;
import com.unipi.gsimos.vistaseat.dto.BookingInfo;
import com.unipi.gsimos.vistaseat.dto.OrderCardDto;
import com.unipi.gsimos.vistaseat.dto.PendingBookingDto;
import com.unipi.gsimos.vistaseat.model.PaymentMethods;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    Long createBooking(PendingBookingDto pendingBookingDto);

    BookingInfo prepareBookingInfo(Long occurrenceId, int requestedTickets);

    void confirmBooking(Long bookingId, PaymentMethods paymentMethod);

    Long countBookingsByVenueAndDateBetween(Long venueId, LocalDate windowStart, LocalDate windowEnd);

    Page<OrderCardDto> getActiveOrdersByUserId(Long userId, Pageable pageable);

    Page<OrderCardDto> getPastOrdersByUserId(Long userId, Pageable pageable);

    List<BookingDto> getLast10Bookings();

}
