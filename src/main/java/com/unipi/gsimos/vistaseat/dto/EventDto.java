package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {

    private Long id;

    @NotBlank(message = "Event name is required")
    @Size(min = 2, max = 100, message = "Event name must be between 2 and 100 characters")
    // Regex allows letters, digits, space, hyphen, apostrophe, period, comma, ampersand
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-',.&]+$", message = "Event name contains invalid characters")
    private String name;

    // Dropdown menu with the available event types
    @NotNull(message = "Event type is required")
    private EventType eventType;

    private String description;

    // Dropdown menu with the available Venues
    @NotNull(message = "Please select one of the available Venues")
    private Long venueId;

    private Long adminId;

    // Derived data needed by the client/UI layer. Keeps count of occurrences for every Event.
    private Long occurrenceCount;
}
