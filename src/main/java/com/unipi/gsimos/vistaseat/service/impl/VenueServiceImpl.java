package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.VenueDto;
import com.unipi.gsimos.vistaseat.mapper.VenueMapper;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.UserRole;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.VenueService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final VenueMapper venueMapper;
    private final EventRepository eventRepository;

    public VenueServiceImpl(VenueRepository venueRepository, UserRepository userRepository, VenueMapper venueMapper, EventRepository eventRepository) {
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.venueMapper = venueMapper;
        this.eventRepository = eventRepository;
    }

    @Override
    public VenueDto createVenue(VenueDto dto) {
        User admin =  userRepository.findById(dto.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Venue name is unique
        if (venueRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("A Venue with this name already exists");
        }

        // Make sure that only admins can create venues
        if (admin.getRole() != UserRole.DOMAIN_ADMIN) {
            throw new RuntimeException("Only admins can create venues.");
        }

        Venue venue = venueMapper.toEntity(dto, admin);
        Venue savedVenue = venueRepository.save(venue);
        return venueMapper.toDto(savedVenue);

    }

    @Override
    public Long countVenues() {
        return venueRepository.count();
    }

    @Override
    public Page<VenueDto> getAllVenues(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("capacity").descending());
        Page<Venue> venues = venueRepository.findAll(pageable);
        List<VenueDto> venuesWithEventCount = calculateEventCountOfVenue(venues.getContent());
        return new PageImpl<>(venuesWithEventCount, pageable, venues.getTotalElements());
        //return venues.map(venueMapper::toDto);
    }

    @Override
    public Page<VenueDto> searchVenueByName(String searchQuery, Pageable pageable) {
        Page<Venue> venues = venueRepository.findVenueByNameContainingIgnoreCase(searchQuery, pageable);
        List<VenueDto> venuesWithEventCount = calculateEventCountOfVenue(venues.getContent());
        return new PageImpl<>(venuesWithEventCount, pageable, venues.getTotalElements());
        /*return venueRepository.findVenueByNameContainingIgnoreCase(searchQuery, pageable)
                .map(venueMapper::toDto);*/
    }

    @Override
    @Transactional
    public void deleteVenue(Long VenueId) {
        Venue venue = venueRepository.findById(VenueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        if (!venue.getEvents().isEmpty()) {
            throw new IllegalStateException("Cannot delete venue: it is associated with existing events.");
        }

        venueRepository.deleteById(VenueId);
    }

    /**
     * Helper method that maps a list of {@link Venue} entities to their corresponding {@link VenueDto} representations,
     * enriching each DTO with the count of events for the specific venue.
     * <p>
     * This method calls {@code venueMapper.toDto()} for basic mapping and then appends the
     * event count retrieved from {@link EventRepository} for each venue.
     *
     * @param venues the list of Venue entities to convert and enrich
     * @return a list of VenueDto objects, each including its event count
     */
    private List<VenueDto> calculateEventCountOfVenue(List<Venue> venues) {
        return venues.stream()
                .map(venue -> {
                    VenueDto venueDto = venueMapper.toDto(venue);
                    long eventCount = eventRepository.countByVenueId(venue.getId());
                    venueDto.setEventCount(eventCount);
                    return venueDto;
                })
                .toList();
    }
}
