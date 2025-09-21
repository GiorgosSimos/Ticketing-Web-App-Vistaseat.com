package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.BookingDto;
import com.unipi.gsimos.vistaseat.dto.BookingInfo;
import com.unipi.gsimos.vistaseat.dto.OrderCardDto;
import com.unipi.gsimos.vistaseat.dto.PendingBookingDto;
import com.unipi.gsimos.vistaseat.model.PaymentMethods;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    Long createBooking(PendingBookingDto pendingBookingDto);

    void deleteBooking(Long id);

    BookingInfo prepareBookingInfo(Long occurrenceId, int requestedTickets);

    void confirmBooking(Long bookingId, PaymentMethods paymentMethod);

    void rescheduleBooking (Long bookingId, Long newOccurrenceId);

    void cancelBookingAndRefund (Long bookingId);

    Long countBookingsByVenueAndDateBetween(Long venueId, LocalDate windowStart, LocalDate windowEnd);

    Page<OrderCardDto> getActiveOrdersByUserId(Long userId, Pageable pageable);

    Page<OrderCardDto> getPastOrdersByUserId(Long userId, Pageable pageable);

    List<BookingDto> getLast10Bookings();

    Page<BookingDto> getAllBookings(Pageable pageable);

    Page<BookingDto> getAllBookingsByEventName(String eventName, Pageable pageable);

    Page<BookingDto> getAllBookingsByEmail(String email, Pageable pageable);

    Page<BookingDto> getAllBookingsByVenueName(String venueName, Pageable pageable);

    // Returns one row. Used Page for UI uniformity
    Page<BookingDto> getBookingById(Long bookingId, Pageable pageable);

    Page<BookingDto> getBookingsByEventDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);

}
