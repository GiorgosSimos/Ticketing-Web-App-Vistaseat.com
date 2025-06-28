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
     * Fetches all occurrences for a given venue that start before or at the given window end.
     * <p>
     * Used to retrieve potential conflicting events when checking venue availability
     * against a candidate time slot. The returned occurrences are further filtered
     * in the service layer to confirm actual overlaps based on event durations and buffers.
     *
     * @param venueId   the ID of the venue
     * @param windowEnd the upper limit of the candidate time window
     * @return list of EventOccurrences starting before or at the specified window end
     */
    @Query("""
            SELECT o
            FROM EventOccurrence o
            WHERE o.event.venue.id = :venueId
            AND o.eventDate <= :windowEnd
            """)
    List<EventOccurrence> findOccurrencesInWindow(Long venueId, LocalDateTime windowEnd);
}
