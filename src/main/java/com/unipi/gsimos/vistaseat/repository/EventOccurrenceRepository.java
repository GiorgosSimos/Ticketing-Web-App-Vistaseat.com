package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.EventOccurrence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventOccurrenceRepository extends JpaRepository<EventOccurrence, Long> {

    long countEventOccurrenceByEventId(long eventId);

    Page<EventOccurrence> findAllByEventId(Long eventId, Pageable pageable);
}
