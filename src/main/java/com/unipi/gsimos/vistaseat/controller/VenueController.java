package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.VenueDto;
import com.unipi.gsimos.vistaseat.service.VenueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apis/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping("/register")
    public ResponseEntity<VenueDto> createVenue(@RequestBody VenueDto venueDto) {
        VenueDto savedVenueDto = venueService.createVenue(venueDto);
        return new ResponseEntity<>(savedVenueDto, HttpStatus.CREATED);
    }
}
