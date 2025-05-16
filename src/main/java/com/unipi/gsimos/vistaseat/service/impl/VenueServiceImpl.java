package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.VenueDto;
import com.unipi.gsimos.vistaseat.mapper.VenueMapper;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.UserRole;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.VenueService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final VenueMapper venueMapper;

    public VenueServiceImpl(VenueRepository venueRepository, UserRepository userRepository, VenueMapper venueMapper) {
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.venueMapper = venueMapper;
    }

    @Override
    public VenueDto createVenue(VenueDto dto) {
        User admin =  userRepository.findById(dto.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

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
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Venue> venues = venueRepository.findAll(pageable);
        return venues.map(venueMapper::toDto);
    }

    @Override
    public Page<VenueDto> searchVenueByName(String searchQuery, Pageable pageable) {
        return venueRepository.findVenueByNameContainingIgnoreCase(searchQuery, pageable)
                .map(venueMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteVenue(Long VenueId) {
        Venue venue = venueRepository.findById(VenueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        // prevent orphaned events - TODO User friendly exceptions
        if (!venue.getEvents().isEmpty()) {
            throw new IllegalStateException("Cannot delete venue: it is associated with existing events.");
        }

        venueRepository.deleteById(VenueId);
    }
}
