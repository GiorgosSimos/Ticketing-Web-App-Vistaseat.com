package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.EventOccurrenceCardDto;
import com.unipi.gsimos.vistaseat.dto.EventOccurrenceDto;
import com.unipi.gsimos.vistaseat.mapper.EventOccurrenceMapper;
import com.unipi.gsimos.vistaseat.model.EventOccurrence;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.service.EventOccurrenceService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EventOccurrenceServiceImpl implements EventOccurrenceService {

    //New occurrences must have a 30-minute window before or after scheduled occurrences
    private static final long BUFFER_MINUTES = 30;
    public final EventOccurrenceRepository eventOccurrenceRepository;
    public final BookingRepository bookingRepository;
    public final EventOccurrenceMapper eventOccurrenceMapper;

    @Override
    @Transactional
    public void createEventOccurrence(EventOccurrenceDto eventOccurrenceDto, Venue eventVenue) {

        // Check if the venue is available
        if (!isVenueAvailable(eventVenue,
                eventOccurrenceDto.getEventDateTime(),
                eventOccurrenceDto.getDuration(),
                null)) {
            throw new IllegalStateException("Venue "+eventVenue.getName()+ " is not available at that time");
        }

        EventOccurrence eventOccurrence = eventOccurrenceMapper.toEntity(eventOccurrenceDto);
        eventOccurrenceRepository.save(eventOccurrence);
    }

    @Override
    @Transactional
    public void updateEventOccurrence(EventOccurrenceDto eventOccurrenceDto, Venue eventVenue) {

        // Before making the update check if the venue is available
        if (!isVenueAvailable(eventVenue,
                eventOccurrenceDto.getEventDateTime(),
                eventOccurrenceDto.getDuration(),
                eventOccurrenceDto.getId())) {
            throw new IllegalStateException("Venue "+eventVenue.getName()+ " is not available at that time");
        }

        EventOccurrence occurrence = eventOccurrenceRepository.findById(eventOccurrenceDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Event occurrence not found"));

        occurrence.setEventDate(eventOccurrenceDto.getEventDateTime());
        occurrence.setPrice(eventOccurrenceDto.getPrice());
        occurrence.setDuration(eventOccurrenceDto.getDuration());
        occurrence.setAvailableSeats(eventOccurrenceDto.getAvailableSeats());

        eventOccurrenceRepository.save(occurrence);
    }

    @Override
    @Transactional
    public void deleteEventOccurrence(Long eventOccurrenceId) {
       EventOccurrence eventOccurrence = eventOccurrenceRepository.findById(eventOccurrenceId)
               .orElseThrow(()-> new EntityNotFoundException(
                       "Occurrence with id: "+eventOccurrenceId + " not found"));

       if (!eventOccurrence.getBookings().isEmpty()) {
           throw new EntityExistsException("Cannot delete occurrence: it is associated with existing bookings.");
       }

       eventOccurrenceRepository.delete(eventOccurrence);
    }

    @Override
    public Page<EventOccurrenceDto> getOccurrencesByEventId(Long eventId, Pageable pageable) {
        Page<EventOccurrence> eventOccurrences = eventOccurrenceRepository.findAllByEventId(eventId, pageable);
        List<EventOccurrenceDto> occurrencesWithBookingCount = calculateBookingCountOfOccurrences(eventOccurrences.getContent());
        return new PageImpl<>(occurrencesWithBookingCount, pageable, eventOccurrences.getTotalElements());
    }

    @Override
    public Page<EventOccurrenceDto> getOccurrencesByVenueIdAndDate(Long venueId,
                                                                   @Nullable LocalDate fromDate,
                                                                   @Nullable LocalDate toDate,
                                                                   Pageable pageable) {
        Page<EventOccurrence> eventOccurrences;

        if (fromDate == null && toDate == null) {
            eventOccurrences = eventOccurrenceRepository
                    .findByEvent_Venue_IdOrderByEventDateAsc(venueId, pageable);
        } else {
            LocalDateTime start = (fromDate != null ?
                    fromDate.atStartOfDay() : LocalDate.of(2000,1,1).atStartOfDay());

            LocalDateTime end = (toDate != null ?
                    toDate.plusDays(1).atStartOfDay() : LocalDate.of(2100,1,1).atStartOfDay());

            eventOccurrences = eventOccurrenceRepository
                    .findByEvent_Venue_IdAndEventDateBetweenOrderByEventDateAsc(
                            venueId, start, end, pageable);
        }

        List<EventOccurrenceDto> occurrencesWithBookingCount = calculateBookingCountOfOccurrences(
                eventOccurrences.getContent());
        return new PageImpl<>(occurrencesWithBookingCount, pageable, eventOccurrences.getTotalElements());
    }

    @Override
    public List<EventOccurrenceCardDto> getOccurrencesByEventId(Long eventId) {

        List<EventOccurrence> occurrences = eventOccurrenceRepository.findAllByEventId(eventId);

        return occurrences.stream().map(EventOccurrenceCardDto::from).toList();

    }

    @Override
    public List<EventOccurrenceCardDto> getUpcomingOccurrencesByEventIdAndDateRange(Long eventId,
                                                                                    LocalDate from,
                                                                                    LocalDate to) {
        boolean isDateFilter = from != null && to != null;

        LocalDateTime fromDate = isDateFilter ? from.atStartOfDay() : null;
        LocalDateTime toDate = isDateFilter ? to.plusDays(1).atStartOfDay().minusNanos(1) : null;

        List<EventOccurrence> eventOccurrences;

        if (isDateFilter) {
            eventOccurrences = eventOccurrenceRepository.findUpcomingOccurrencesInWindow(eventId, fromDate, toDate);
        } else {
            eventOccurrences = eventOccurrenceRepository.findUpcomingOccurrences(eventId);
        }

        return  eventOccurrences.stream().map(EventOccurrenceCardDto::from).toList();
    }

    /**
     * Helper method that maps a list of {@link EventOccurrence} entities to their corresponding
     * @link {EventOccurrenceDto} representations, enriching each DTO with the number of associated bookings.
     * <p>
     * This method calls {@code eventOccurrenceMapper.toDto()} for basic mapping and then appends the
     * booking count retrieved from {@link BookingRepository} for each occurrence.
     *
     * @param eventOccurrences the list of EventOccurrence entities to convert and enrich
     * @return a list of EventOccurrenceDto objects, each including its booking count
     */
    private List<EventOccurrenceDto> calculateBookingCountOfOccurrences(List<EventOccurrence> eventOccurrences) {
        return eventOccurrences.stream()
                .map(eventOccurrence -> {
                    EventOccurrenceDto occurrenceDto = eventOccurrenceMapper.toDto(eventOccurrence);
                    long count = bookingRepository.countBookingsByEventOccurrenceId(eventOccurrence.getId());
                    occurrenceDto.setBookingCount(count);
                    return occurrenceDto;
                })
                .toList();
    }

    /**
     * @param venue            venue we want to book
     * @param newOccurrenceStart         start date-time of the new occurrence
     * @param newOccurrenceDuration   duration in minutes of the new occurrence
     * @return true if venue is free (incl. 30-min buffer), false otherwise
     */
    public boolean isVenueAvailable(Venue venue,
                                    LocalDateTime newOccurrenceStart,
                                    int newOccurrenceDuration,
                                    Long skipId) {

        // Setting a 3-day window to search for potential clashes
        LocalDate newOccurrenceDate = newOccurrenceStart.toLocalDate();
        LocalDateTime windowStart = newOccurrenceDate.atStartOfDay().minusDays(1);
        LocalDateTime windowEnd = windowStart.plusDays(3);

        // Fetch occurrences within the 3-day window that could overlap with the new occurrence
        List<EventOccurrence> possibleClashes = eventOccurrenceRepository.findOccurrencesInWindow(
                venue.getId(), windowStart, windowEnd, skipId);

        LocalDateTime newOccurrenceEndBuffered = newOccurrenceStart
                                        .plusMinutes(newOccurrenceDuration)
                                        .plusMinutes(BUFFER_MINUTES);

        // Test each possible clash with the new occurrence
        boolean overlap = possibleClashes.stream().anyMatch(currentOccurrence ->{
            LocalDateTime currentOccurrenceStart = currentOccurrence.getEventDate();

            LocalDateTime currentOccurrenceEnd = currentOccurrence.getEventDate()
                                                  .plusMinutes(currentOccurrence.getDuration())
                                                  .plusMinutes(BUFFER_MINUTES);

            // If it returns true, there is an overlap
            // We ask, “Does the new show’s start come before the other show’s (end + 30 min)
            // and does the new show’s (end + 30 min) come after the other show’s start?”
            // if both are true, the times overlap.
            return newOccurrenceStart.isBefore(currentOccurrenceEnd) &&
                    newOccurrenceEndBuffered.isAfter(currentOccurrenceStart);
        });
        return !overlap;//The venue is available if nothing overlapped
    }

    /**
     * Helper service that calculates the total number of bookings for the given collection
     * of {@link EventOccurrenceDto} objects.
     *
     * <p>Each element’s {@code bookingCount} may be {@code null}; such
     * values are treated as zero and ignored in the sum.</p>
     *
     * @param occurrencesList a <strong>non-{@code null}</strong> list of event-occurrence DTOs
     * @return the sum of all non-null {@code bookingCount} values, or {@code 0} if the list is empty
     * @throws NullPointerException if {@code occurrences} is {@code null}
     */
    public long sumBookingCounts(List<EventOccurrenceDto> occurrencesList) {
        return occurrencesList.stream()
                .map(EventOccurrenceDto::getBookingCount)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
    }
}

