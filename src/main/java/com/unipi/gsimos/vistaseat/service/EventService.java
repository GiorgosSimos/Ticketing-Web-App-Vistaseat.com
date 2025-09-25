package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.CategoriesEventCardDto;
import com.unipi.gsimos.vistaseat.dto.EventDto;
import com.unipi.gsimos.vistaseat.model.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;

public interface EventService {

    EventDto createEvent(EventDto eventDto);

    void deleteEvent(Long eventId);

    Page<EventDto> getAllEvents (Pageable pageable);

    Page<EventDto> getEventsByName (String searchQuery, Pageable pageable);

    Page<EventDto> getEventsByNameAndVenue(String searchQuery, Long venueId, Pageable pageable);

    Page<EventDto> getEventsByVenueId(Long venueId, Pageable pageable);

    Page<EventDto> getEventsByEventType (EventType eventType, Pageable  pageable);

    Page<EventDto> getEventsByNameAndType (String searchQuery, EventType eventType, Pageable pageable);

    List<CategoriesEventCardDto> getEvents(EventType eventType,
                                           @Nullable String eventName,
                                           @Nullable LocalDate from,
                                           @Nullable LocalDate to);

    List<CategoriesEventCardDto> getVenueEvents(Long venueId,
                                                @Nullable String eventName,
                                                @Nullable LocalDate from,
                                                @Nullable LocalDate to);
}
