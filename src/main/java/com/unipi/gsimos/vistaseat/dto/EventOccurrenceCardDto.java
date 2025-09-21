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
        int venueZipcode,
        boolean soldOut,
        int initialSeats,
        int availableSeats,
        AvailabilityLevel availabilityLevel) {

    public static float getAvailability(EventOccurrence occurrence) {

        int capacity = occurrence.getAvailableSeats();

        if (occurrence.isSoldOut() || capacity == 0) {
            return 0;
        }
        return occurrence.getRemainingSeats() / (float)capacity;
    }

    public static AvailabilityLevel level(EventOccurrence occurrence) {
        float pct = getAvailability(occurrence) * 100f;

        if (pct == 0f)               return AvailabilityLevel.SOLD_OUT;
        if (pct <= 24f)              return AvailabilityLevel.LOW;
        if (pct <= 49f)              return AvailabilityLevel.MEDIUM;
        return AvailabilityLevel.HIGH;
    }

    public enum AvailabilityLevel {
        HIGH   ("Available",     "bg-success", "bg-high"),
        MEDIUM ("Limited",   "bg-secondary text-light", "bg-medium"),
        LOW    ("Few Left",      "bg-warning text-dark", "bg-low"),
        SOLD_OUT("Sold Out","bg-danger", "bg-sold-out");

        public final String label;
        public final String badge;
        public final String color;


        AvailabilityLevel(String label, String badge, String color) {
            this.label = label;
            this.badge   = badge;
            this.color   = color;
        }
    }

    public static EventOccurrenceCardDto from(EventOccurrence eventOccurrence) {

        return new EventOccurrenceCardDto(
                eventOccurrence.getId(),
                eventOccurrence.getEventDate(),
                eventOccurrence.getPrice(),
                eventOccurrence.getEvent().getVenue().getName(),
                eventOccurrence.getEvent().getVenue().getStreet(),
                eventOccurrence.getEvent().getVenue().getNumber(),
                eventOccurrence.getEvent().getVenue().getCity(),
                eventOccurrence.getEvent().getVenue().getZipcode(),
                eventOccurrence.isSoldOut(),
                eventOccurrence.getAvailableSeats(),
                eventOccurrence.getRemainingSeats(),
                level(eventOccurrence)
        );
    }
}
