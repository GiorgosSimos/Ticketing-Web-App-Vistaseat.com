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
        @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$")
        String phoneNumber) {
}
