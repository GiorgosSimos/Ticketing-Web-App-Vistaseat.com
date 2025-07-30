package com.unipi.gsimos.vistaseat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PendingBookingDto(
        Long occurrenceId,
        int numberOfTickets,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email String email,
        @NotBlank
        @Pattern(regexp = "^\\d{10}$")
        String phoneNumber) {
}
