package com.unipi.gsimos.vistaseat.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventOccurrenceDto {

    private Long id;

    private LocalDateTime eventDateTime;

    @Min(value = 0, message = "Event price cannot be negative")
    private BigDecimal price;

    @Min(value = 0, message = "Duration of an event cannot be negative")
    private int duration;

    //WARNING: Initialization of available seats should not exceed the capacity of the corresponding venue
    private int availableSeats;

    private Long eventId;

    private int bookingCount;
}
