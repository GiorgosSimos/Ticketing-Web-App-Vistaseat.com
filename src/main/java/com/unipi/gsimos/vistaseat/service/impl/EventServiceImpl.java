package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.EventDto;
import com.unipi.gsimos.vistaseat.mapper.EventMapper;
import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.EventType;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.service.EventService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventOccurrenceRepository eventOccurrenceRepository;

    @Override
    @Transactional
    public EventDto createEvent(@NotNull EventDto eventDto) {

        if (eventRepository.existsByNameAndEventTypeAndVenueId(
                eventDto.getName(),
                eventDto.getEventType(),
                eventDto.getVenueId())) {
            throw new EntityExistsException("An event with the same name, type, and venue already exists.");
        }

        Event event = eventMapper.toEntity(eventDto);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDto(savedEvent);
    }

    @Override
    public Page<EventDto> getAllEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventRepository.findAll(pageable);
        List<EventDto> eventsWithOccurrenceCount = calculateEventOccurrenceCount(events.getContent());
        return new PageImpl<>(eventsWithOccurrenceCount, pageable, events.getTotalElements());
        //return events.map(eventMapper::toDto);
    }

    @Override
    public Page<EventDto> getEventsByName(String searchQuery, Pageable pageable) {
        Page<Event> events = eventRepository.findEventByNameContainingIgnoreCase(searchQuery, pageable);
        List<EventDto> eventsWithOccurrenceCount = calculateEventOccurrenceCount(events.getContent());
        return new PageImpl<>(eventsWithOccurrenceCount, pageable, events.getTotalElements());
        /*return eventRepository.findEventByNameContainingIgnoreCase(searchQuery, pageable)
                .map(eventMapper::toDto);*/
    }

    @Override
    public Page<EventDto> getEventsByVenueId(Long venueId, Pageable pageable) {
        Page<Event> events = eventRepository.findAllByVenueId(venueId, pageable);
        List<EventDto> eventsWithOccurrenceCount = calculateEventOccurrenceCount(events.getContent());
        return new PageImpl<>(eventsWithOccurrenceCount, pageable, events.getTotalElements());
    }

    @Override
    public Page<EventDto> getEventsByNameAndVenue(String searchQuery, Long venueId , Pageable pageable) {
        Page<Event> events = eventRepository.findEventByNameContainingIgnoreCaseAndVenueId(searchQuery, venueId, pageable);
        List<EventDto> eventsWithOccurrenceCount = calculateEventOccurrenceCount(events.getContent());
        return new PageImpl<>(eventsWithOccurrenceCount, pageable, events.getTotalElements());
    }

    @Override
    public Page<EventDto> getEventsByEventType(EventType eventType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventRepository.findAllByEventType(eventType, pageable);
        return events.map(eventMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(()-> new EntityNotFoundException("Event not found with id: " + eventId));

        if (!event.getOccurrences().isEmpty()) {
            throw new EntityExistsException("Cannot delete event: it is associated with existing occurrences.");
        }

        eventRepository.delete(event);
    }

    /**
     * Helper method that maps a list of {@link Event} entities to their corresponding {@link EventDto} representations,
     * enriching each DTO with the number of associated event occurrences.
     * <p>
     * This method calls {@code eventMapper.toDto()} for basic mapping and then appends the
     * occurrence count retrieved from {@link EventOccurrenceRepository} for each event.
     *
     * @param events the list of Event entities to convert and enrich
     * @return a list of EventDto objects, each including its occurrence count
     */
    private List<EventDto> calculateEventOccurrenceCount(List<Event> events) {
        return events.stream()
                .map(event -> {
                    EventDto eventDto = eventMapper.toDto(event);
                    long count = eventOccurrenceRepository.countEventOccurrenceByEventId(event.getId());
                    eventDto.setOccurrenceCount(count);
                    return eventDto;
                })
                .toList();
    }
}
