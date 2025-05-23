package com.unipi.gsimos.vistaseat.mapper;

import com.unipi.gsimos.vistaseat.dto.EventDto;
import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component // registers class as a Spring bean so it can be injected
@RequiredArgsConstructor // used only for final or NonNull fields
public class EventMapper {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    public Event toEntity(EventDto eventDto) {
        Event event = new Event();

        event.setName(eventDto.getName());
        event.setEventType(eventDto.getEventType());
        event.setDescription(eventDto.getDescription());

        Venue venue = venueRepository.findById(eventDto.getVenueId())
                .orElseThrow(()-> new IllegalArgumentException("Venue not found"));
        event.setVenue(venue);

        User admin = userRepository.findById(eventDto.getAdminId())
                .orElseThrow(()-> new IllegalArgumentException("Admin not found"));
        event.setManagedBy(admin);

        return event;
    }

    public EventDto toDto(Event event) {
        EventDto eventDto = new EventDto();

        eventDto.setId(event.getId());
        eventDto.setName(event.getName());
        eventDto.setEventType(event.getEventType());
        eventDto.setDescription(event.getDescription());
        if (event.getVenue() != null) {
            eventDto.setVenueId(event.getVenue().getId());
        }

        if (event.getManagedBy() != null) {
            eventDto.setAdminId(event.getManagedBy().getId());
        }

        return eventDto;
    }
}
