package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.EventOccurrence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventOccurrenceCardDto(
        Long occurrenceId,
        LocalDateTime dateTime,
        BigDecimal price,
        String venueName,
        String venueStreet,
        int venueNumber,
        String venueCity,
        int venueZipcode) {

    public static EventOccurrenceCardDto from(EventOccurrence eventOccurrence) {

        return new EventOccurrenceCardDto(
                eventOccurrence.getId(),
                eventOccurrence.getEventDate(),
                eventOccurrence.getPrice(),
                eventOccurrence.getEvent().getVenue().getName(),
                eventOccurrence.getEvent().getVenue().getStreet(),
                eventOccurrence.getEvent().getVenue().getNumber(),
                eventOccurrence.getEvent().getVenue().getCity(),
                eventOccurrence.getEvent().getVenue().getZipcode()
        );
    }
}
