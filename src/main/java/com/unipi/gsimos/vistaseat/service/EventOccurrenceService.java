package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.EventOccurrenceCardDto;
import com.unipi.gsimos.vistaseat.dto.EventOccurrenceDto;
import com.unipi.gsimos.vistaseat.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;

public interface EventOccurrenceService {

    void createEventOccurrence(EventOccurrenceDto eventOccurrenceDto, Venue eventVenue);

    void updateEventOccurrence(EventOccurrenceDto eventOccurrenceDto, Venue eventVenue);

    void deleteEventOccurrence(Long eventOccurrenceId);

    Page<EventOccurrenceDto> getOccurrencesByEventId(Long eventId, Pageable pageable);

    Page<EventOccurrenceDto> getOccurrencesByVenueIdAndDate(
            Long venueId,
            @Nullable LocalDate fromDate,
            @Nullable LocalDate toDate,
            Pageable pageable);

    long sumBookingCounts(List<EventOccurrenceDto> occurrencesList);

    List<EventOccurrenceCardDto> getOccurrencesByEventId(Long eventId);

    // Returns all the occurrences of a specific event within a certain date range
    List<EventOccurrenceCardDto> getUpcomingOccurrencesByEventIdAndDateRange(Long eventId,
                                                                     @Nullable LocalDate from,
                                                                     @Nullable LocalDate to);
}
