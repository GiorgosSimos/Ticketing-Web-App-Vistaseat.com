package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.EventOccurrenceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventOccurrenceService {

    void createEventOccurrence(EventOccurrenceDto eventOccurrenceDto);

    Page<EventOccurrenceDto> getOccurrencesByEventId(Long eventId, Pageable pageable);
}
