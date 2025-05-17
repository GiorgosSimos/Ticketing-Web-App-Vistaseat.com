package com.unipi.gsimos.vistaseat.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VenueDto {

    private Long id;

    @NotBlank(message = "Venue name is required")
    @Size(min = 2, max = 100, message = "Venue name must be between 2 and 100 characters")
    // Regex allows letters, digits, space, hyphen, apostrophe, period, comma, ampersand
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-',.&]+$", message = "Venue name contains invalid characters")
    private String name;

    @NotBlank(message = "Street is required")
    @Size(min = 2, max = 100, message = "Street name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-',.&]+$", message = "Street name contains invalid characters")
    private String street;

    @NotNull(message = "Number is required")
    @Pattern(regexp = "^\\d{3}$", message = "Number must be 3 digits")
    private Integer number;

    @NotNull(message = "Zip code is required")
    // Greek zipcodes (5 digits)
    @Pattern(regexp = "^\\d{5}$", message = "Zip code must be 5 digits")
    private Integer zipcode;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-',.&]+$", message = "City name contains invalid characters")
    private String city;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 100000, message = "Capacity is unreasonably large")
    private Integer capacity;

    private Long adminId;

}
