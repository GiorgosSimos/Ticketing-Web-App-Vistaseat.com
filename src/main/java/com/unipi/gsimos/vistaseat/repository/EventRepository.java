package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByNameAndEventTypeAndVenueId(String name, EventType eventType, Long venueId);

    Page<Event> findEventByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Event> findAllByEventType(EventType eventType, Pageable pageable);
}
