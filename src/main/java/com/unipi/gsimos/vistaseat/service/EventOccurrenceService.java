package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.EventOccurrenceDto;
import com.unipi.gsimos.vistaseat.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventOccurrenceService {

    void createEventOccurrence(EventOccurrenceDto eventOccurrenceDto, Venue eventVenue);

    void updateEventOccurrence(EventOccurrenceDto eventOccurrenceDto, Venue eventVenue);

    void deleteEventOccurrence(Long eventOccurrenceId);

    Page<EventOccurrenceDto> getOccurrencesByEventId(Long eventId, Pageable pageable);
}
