package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Long countBookingsByEventOccurrenceId(Long eventOccurrenceId);

    // Total number of bookings across all occurrences of a given event
    Long countBookingsByEventOccurrence_Event_Id(Long eventId);

    // Total number of bookings across all occurrences scheduled in a given venue
    Long countBookingsByEventOccurrence_Event_Venue_Id(Long venueId);

}
