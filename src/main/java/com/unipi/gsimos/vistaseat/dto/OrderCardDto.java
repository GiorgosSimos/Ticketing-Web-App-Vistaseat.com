package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.Booking;
import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.EventOccurrence;

import java.time.LocalDateTime;

public record OrderCardDto(
       Long bookingId,
       int numberOfTickets,
       String eventImgURL,
       String eventName,
       LocalDateTime eventDateTime,
       String venueName) {

    public static OrderCardDto from(Booking booking) {

        EventOccurrence occurrence = booking.getEventOccurrence();
        Event event = occurrence.getEvent();

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

        return new OrderCardDto(
                booking.getId(),
                booking.getNumberOfTickets(),
                eventThumb,
                event.getName(),
                occurrence.getEventDate(),
                event.getVenue().getName()
        );
    }
}
