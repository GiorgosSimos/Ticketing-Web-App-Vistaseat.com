package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Booking;
import com.unipi.gsimos.vistaseat.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Long countBookingsByEventOccurrenceId(Long eventOccurrenceId);

    // Total number of bookings across all occurrences of a given event
    Long countBookingsByEventOccurrence_Event_Id(Long eventId);

    // Total number of bookings across all occurrences scheduled in a given venue
    Long countByEventOccurrence_Event_Venue_Id(Long eventVenueId);

    Page<Booking> findByEventOccurrenceId(Long eventOccurrenceId, Pageable pageable);

    Page<Booking> findByIdAndEventOccurrenceId(Long bookingId, Long eventOccurrenceId, Pageable pageable);

    // List of bookings across all occurrences of a given event name
    Page<Booking> findByEventOccurrence_Event_NameContainingIgnoreCase(String eventName, Pageable pageable);

    Page<Booking> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<Booking> findByEventOccurrenceIdAndLastNameContainingIgnoreCase(Long eventOccurrenceId, String lastName, Pageable pageable);

    Page<Booking> findByEventOccurrence_Event_Venue_NameContainingIgnoreCase(String eventVenueName, Pageable pageable);

    Page<Booking> findById(Long bookingId, Pageable pageable);

    long countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateAfter(
            Long venueId, LocalDateTime windowStart);

    long countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateBefore(
            Long venueId, LocalDateTime windowEnd);

    // Total number of bookings across all occurrences scheduled in a given venue between a range of dates
    Long countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateBetween(
            Long venueId, LocalDateTime windowStart, LocalDateTime windowEnd);

    List<Booking> findTop10ByOrderByBookingDateDesc();

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) " +
            "FROM Booking b WHERE b.status = :status")
    BigDecimal getTotalRevenueByStatus(@Param("status") BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) " +
            "FROM Booking b " +
            "WHERE b.eventOccurrence.id = :occurrenceId AND b.status = :status")
    BigDecimal getTotalRevenueByOccurrenceIdAndStatus(@Param("occurrenceId") Long occurrenceId,
                                                      @Param("status") BookingStatus status);

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

    @Query("""
       SELECT b
       FROM Booking b
       JOIN b.eventOccurrence eo
       WHERE eo.eventDate BETWEEN :from AND :to
       """)
    Page<Booking> findBookingsByEventDateRange(@Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to,
                                               Pageable pageable);

}
