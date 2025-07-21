package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByNameAndEventTypeAndVenueId(String name, EventType eventType, Long venueId);

    Page<Event> findEventByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Event> findEventByNameContainingIgnoreCaseAndVenueId(String name, Long venueId, Pageable pageable);

    Page<Event> findAllByEventType(EventType eventType, Pageable pageable);

    List<Event> findAllByVenueId(Long venueId);

    List<Event> findTop5ByNameContainingIgnoreCase(String eventName);

    Page<Event> findAllByVenueId(Long venueId, Pageable pageable);

    Long countByVenueId(Long venueId);

    @Query("""
        select distinct e
        from Event e
        where exists (
            select 1 from EventOccurrence o
            where o.event = e
        )
    """)
    List<Event> findAllWithAtLeastOneOccurrence();

    @Query("""
    select distinct e
    from Event e
    where e.eventType = :type
      and exists (
          select 1
          from EventOccurrence o
          where o.event = e
          )
    """)
    List<Event> findAllWithAtLeastOneOccurrenceByType(@Param("type") EventType type);
}
