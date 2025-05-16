package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.VenueDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VenueService {

    VenueDto createVenue(VenueDto dto);

    void  deleteVenue(Long VenueId);

    Long countVenues();

    Page<VenueDto> getAllVenues(int page, int size);

    Page<VenueDto> searchVenueByName(String searchQuery, Pageable pageable);


}
