package com.unipi.gsimos.vistaseat.mapper;

import com.unipi.gsimos.vistaseat.dto.EventOccurrenceDto;
import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.EventOccurrence;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventOccurrenceMapper {

    public final EventRepository eventRepository;

    public EventOccurrence toEntity(EventOccurrenceDto eventOccurrenceDto) {
        EventOccurrence eventOccurrence = new EventOccurrence();

        eventOccurrence.setEventDate(eventOccurrenceDto.getEventDateTime());
        eventOccurrence.setPrice(eventOccurrenceDto.getPrice());
        eventOccurrence.setDuration(eventOccurrenceDto.getDuration());
        eventOccurrence.setAvailableSeats(eventOccurrenceDto.getAvailableSeats());

        Event eventOfOccurrences = eventRepository.findById(eventOccurrenceDto.getEventId())
                .orElseThrow(()-> new IllegalArgumentException("Event not found"));
        eventOccurrence.setEvent(eventOfOccurrences);

        return eventOccurrence;
    }

    public EventOccurrenceDto toDto(EventOccurrence eventOccurrence) {
        EventOccurrenceDto eventOccurrenceDto = new EventOccurrenceDto();

        eventOccurrenceDto.setId(eventOccurrence.getId());
        eventOccurrenceDto.setEventDateTime(eventOccurrence.getEventDate());
        eventOccurrenceDto.setPrice(eventOccurrence.getPrice());
        eventOccurrenceDto.setDuration(eventOccurrence.getDuration());
        eventOccurrenceDto.setAvailableSeats(eventOccurrence.getAvailableSeats());
        eventOccurrenceDto.setEventId(eventOccurrence.getEvent().getId());

        return eventOccurrenceDto;
    }
}
