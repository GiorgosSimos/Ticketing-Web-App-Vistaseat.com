package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.EventOccurrence;

import java.time.LocalDateTime;

// Helper DTO used to slide through the different events in the featured events section in home.html
public record EventCardDto(
        Long id,
        String title,
        String eventType,
        String venueName,
        String thumbnailUrl,
        LocalDateTime firstDate,
        String hrefLink) {

    public static EventCardDto from(Event event) {

        String thumbUrl = switch (event.getEventType()){
            case THEATER -> "/images/theater_play.jpg";
            case CINEMA -> "/images/feature_film.jpg";
            case CONCERT -> "/images/music_concert.jpg";
            case SPORTS -> "/images/sports.jpg";
            case MUSEUM -> "/images/museum_visit.png";
            case ARCHAEOLOGICAL -> "/images/archaeological_site.jpg";
        };

        return new EventCardDto(
                event.getId(),
                event.getName(),
                event.getEventType().toString(),
                event.getVenue().getName(),
                thumbUrl,
                event.getOccurrences().stream()
                    .map(EventOccurrence::getEventDate)
                    .min(LocalDateTime::compareTo)
                        .orElse(null),
                "/api/events/" + event.getId()
        );
    }
}
