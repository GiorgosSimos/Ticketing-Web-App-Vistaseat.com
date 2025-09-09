package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Long countBookingsByEventOccurrenceId(Long eventOccurrenceId);

    // Total number of bookings across all occurrences of a given event
    Long countBookingsByEventOccurrence_Event_Id(Long eventId);

    // Total number of bookings across all occurrences scheduled in a given venue
    Long countByEventOccurrence_Event_Venue_Id(Long eventVenueId);

    long countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateAfter(
            Long venueId, LocalDateTime windowStart);

    long countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateBefore(
            Long venueId, LocalDateTime windowEnd);

    // Total number of bookings across all occurrences scheduled in a given venue between a range of dates
    Long countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateBetween(
            Long venueId, LocalDateTime windowStart, LocalDateTime windowEnd);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.eventOccurrence eo " +
            "WHERE b.user.id = :userId " +
            "AND eo.eventDate >= CURRENT_TIMESTAMP")
    Page<Booking> findActiveBookingsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.eventOccurrence eo " +
            "WHERE b.user.id = :userId " +
            "AND eo.eventDate < CURRENT_TIMESTAMP")
    Page<Booking> findPastBookingsByUserId(@Param("userId") Long userId, Pageable pageable);

    // BookingRepository
    @Modifying
    @Query("""
       UPDATE Booking b
          SET b.status = 'CANCELLED'
        WHERE b.status = 'PENDING'
          AND b.expiresAt < :now
""")
    int expireOlderThan(LocalDateTime now);


}
