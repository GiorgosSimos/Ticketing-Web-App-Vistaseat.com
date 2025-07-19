package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.Venue;

public record VenueCardDto(Long id,
                           String thumbnailUrl,
                           String name,
                           String street,
                           int number,
                           String city,
                           int zipcode,
                           String hrefLink) {

    public static VenueCardDto from(Venue venue) {

        String thumbUrl = "/images/generic_venue.png";

        return new VenueCardDto(
                venue.getId(),
                thumbUrl,
                venue.getName(),
                venue.getStreet(),
                venue.getNumber(),
                venue.getCity(),
                venue.getZipcode(),
                "api/venues/" + venue.getId()
        );
    }
}
