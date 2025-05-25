package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.EventDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {

    EventDto createEvent(EventDto eventDto);

    Page<EventDto> getAllEvents (int page, int size);

    Page<EventDto> getEventsByName (String searchQuery, Pageable pageable);
}
