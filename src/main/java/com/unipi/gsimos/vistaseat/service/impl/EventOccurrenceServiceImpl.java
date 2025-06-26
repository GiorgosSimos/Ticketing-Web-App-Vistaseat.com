package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.EventOccurrenceDto;
import com.unipi.gsimos.vistaseat.mapper.EventOccurrenceMapper;
import com.unipi.gsimos.vistaseat.model.EventOccurrence;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.service.EventOccurrenceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventOccurrenceServiceImpl implements EventOccurrenceService {

    public final EventOccurrenceRepository eventOccurrenceRepository;
    public final BookingRepository bookingRepository;
    public final EventOccurrenceMapper eventOccurrenceMapper;

    @Override
    @Transactional
    //TODO Check first if the venue is vacant
    public void createEventOccurrence(EventOccurrenceDto eventOccurrenceDto) {

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
     * Helper method that maps a list of {@link EventOccurrence} entities to their corresponding {@link EventOccurrenceDto} representations,
     * enriching each DTO with the number of associated bookings.
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
}
