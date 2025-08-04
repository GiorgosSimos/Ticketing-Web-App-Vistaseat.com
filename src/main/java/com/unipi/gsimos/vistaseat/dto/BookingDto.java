package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.BookingStatus;
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
public class BookingDto {

    private Long id;

    private Long occurrenceId;
    private Long eventId;
    private String eventName;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime bookingDate;
    private int numberOfTickets;
    private BigDecimal ticketsPrice;
    private BigDecimal serviceFee;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime expiresAt;
}
