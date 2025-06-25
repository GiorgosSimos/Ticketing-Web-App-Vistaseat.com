package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.EventOccurrenceDto;
import com.unipi.gsimos.vistaseat.mapper.EventOccurrenceMapper;
import com.unipi.gsimos.vistaseat.model.EventOccurrence;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.service.EventOccurrenceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventOccurrenceServiceImpl implements EventOccurrenceService {

    public final EventOccurrenceRepository eventOccurrenceRepository;
    public final EventOccurrenceMapper eventOccurrenceMapper;

    @Override
    @Transactional
    //TODO Check first if the venue is vacant
    public void createEventOccurrence(EventOccurrenceDto eventOccurrenceDto) {

        EventOccurrence eventOccurrence = eventOccurrenceMapper.toEntity(eventOccurrenceDto);
        eventOccurrenceRepository.save(eventOccurrence);

    }
}
