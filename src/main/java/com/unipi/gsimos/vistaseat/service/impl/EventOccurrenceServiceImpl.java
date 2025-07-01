package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.EventOccurrenceDto;
import com.unipi.gsimos.vistaseat.mapper.EventOccurrenceMapper;
import com.unipi.gsimos.vistaseat.model.EventOccurrence;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.service.EventOccurrenceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

        if (!isVenueAvailable(eventVenue, eventOccurrenceDto.getEventDateTime(), eventOccurrenceDto.getDuration())) {
            throw new IllegalStateException("Venue "+eventVenue.getName()+ " is not available at that time");
        }

        EventOccurrence eventOccurrence = eventOccurrenceMapper.toEntity(eventOccurrenceDto);
        eventOccurrenceRepository.save(eventOccurrence);
    }

    @Override
    public Page<EventOccurrenceDto> getOccurrencesByEventId(Long eventId, Pageable pageable) {
        Page<EventOccurrence> eventOccurrences = eventOccurrenceRepository.findAllByEventId(eventId, pageable);
        List<EventOccurrenceDto> occurrencesWithBookingCount = calculateBookingCountOfOccurrences(eventOccurrences.getContent());
        return new PageImpl<>(occurrencesWithBookingCount, pageable, eventOccurrences.getTotalElements());
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
    public boolean isVenueAvailable(Venue venue, LocalDateTime newOccurrenceStart, int newOccurrenceDuration) {

        // Setting a 3-day window to search for potential clashes
        LocalDate newOccurrenceDate = newOccurrenceStart.toLocalDate();
        LocalDateTime windowStart = newOccurrenceDate.atStartOfDay().minusDays(1);
        LocalDateTime windowEnd = windowStart.plusDays(3);

        // Fetch occurrences within the 3-day window that could overlap with the new occurrence
        List<EventOccurrence> possibleClashes = eventOccurrenceRepository.findOccurrencesInWindow(
                venue.getId(), windowStart, windowEnd);

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
}
