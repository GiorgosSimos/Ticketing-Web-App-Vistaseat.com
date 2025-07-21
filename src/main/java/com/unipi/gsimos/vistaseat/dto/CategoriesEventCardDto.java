package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.EventOccurrence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Helper DTO used to map the different event categories in api/events/categories
public record CategoriesEventCardDto(
        Long eventId,
        String eventTitle,
        String eventType,
        String eventDescription,
        String eventImageURL,
        String eventVenueName,
        LocalDateTime fromDate,
        Integer duration,
        BigDecimal price,
        String hrefLink) {

    public static CategoriesEventCardDto from(Event event) {

        String eventThumb;

        switch (event.getEventType()) {
            case THEATER ->  eventThumb = "/images/theater_play.jpg";
            case CINEMA ->   eventThumb = "/images/feature_film.jpg";
            case CONCERT ->  eventThumb = "/images/music_concert.jpg";
            case SPORTS ->   eventThumb = "/images/sports.jpg";
            case MUSEUM ->  eventThumb = "/images/museum_visit.png";
            case ARCHAEOLOGICAL -> eventThumb = "/images/archaeological_site.jpg";
            default -> eventThumb = "/images/events_generic.jpg";
        }

        return new CategoriesEventCardDto(
                event.getId(),
                event.getName(),
                event.getEventType().toString(),
                event.getDescription(),
                eventThumb,
                event.getVenue().getName(),
                event.getOccurrences().stream()
                     .map(EventOccurrence::getEventDate)
                     .min(LocalDateTime::compareTo).orElse(null),
                event.getOccurrences().stream()
                     .map(EventOccurrence::getDuration)
                     .mapToInt(Integer::intValue)
                     .min().orElse(0),
                event.getOccurrences().stream()
                     .map(EventOccurrence::getPrice)
                     .min(BigDecimal::compareTo)
                     .orElse(BigDecimal.ZERO),
                "/api/events/" + event.getId()
        );
    }
}
