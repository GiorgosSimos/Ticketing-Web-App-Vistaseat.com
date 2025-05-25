package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.EventOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventOccurrenceRepository extends JpaRepository<EventOccurrence, Long> {

    long countEventOccurrenceByEventId(long eventId);
}
