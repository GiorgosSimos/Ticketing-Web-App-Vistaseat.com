package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.EventOccurrence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface EventOccurrenceRepository extends JpaRepository<EventOccurrence, Long> {

    long countEventOccurrenceByEventId(long eventId);

    Page<EventOccurrence> findAllByEventId(Long eventId, Pageable pageable);

    /**
     * Retrieves every {@link EventOccurrence} that *starts* within the three-day
     * window centred on a candidate date for the given venue.
     *
     * <p>The window is expressed as <strong>[dayStart&nbsp;…&nbsp;nextDayStart)</strong>:
     *
     * <ul>
     *   <li>{@code dayStart} 00:00 of the **day before** the candidate date&nbsp;(inclusive)</li>
     *   <li>{@code nextDayStart} 00:00 of the **day after** the candidate date&nbsp;(exclusive)</li>
     * </ul>
     *
     * Fetching only these rows keeps the result set small; the service layer then
     * applies the 30-minute buffer/duration logic to decide whether any of the
     * returned occurrences actually collide with the new slot.
     *
     * @param venueId       ID of the venue being checked
     * @param dayStart      inclusive lower bound of the window (previous-day 00:00)
     * @param nextDayStart  exclusive upper bound of the window (next-day 00:00)
     * @return all occurrences whose {@code eventDate} falls inside the window
     */
    @Query("""
            SELECT o
            FROM EventOccurrence o
            WHERE o.event.venue.id = :venueId
            AND o.eventDate >= :dayStart
            AND o.eventDate < :nextDayStart
           """)
    List<EventOccurrence> findOccurrencesInWindow(Long venueId,
                                                  LocalDateTime dayStart,
                                                  LocalDateTime nextDayStart);
}
