package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.VenueDto;

public interface VenueService {

    VenueDto createVenue(VenueDto dto);

    Long countVenues();
}
