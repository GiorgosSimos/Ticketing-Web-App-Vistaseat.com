package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    @Override
    public Long countBookingsByVenueAndDateBetween(Long venueId, LocalDate windowStart, LocalDate windowEnd) {

        long bookingCount;

        if (windowEnd == null && windowStart == null) {
            bookingCount = bookingRepository.countByEventOccurrence_Event_Venue_Id(venueId);
        } else if (windowStart == null) {
            bookingCount = bookingRepository.countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateBefore(
                            venueId,
                            windowEnd.plusDays(1).atStartOfDay());
        } else if (windowEnd == null ) {
            bookingCount = bookingRepository.countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateAfter(
                            venueId,
                            windowStart.plusDays(1).atStartOfDay());
        } else {
            bookingCount = bookingRepository.countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateBetween(
                            venueId,
                            windowStart.atStartOfDay(),
                            windowEnd.plusDays(1).atStartOfDay());
        }
        return bookingCount;
    }
}
