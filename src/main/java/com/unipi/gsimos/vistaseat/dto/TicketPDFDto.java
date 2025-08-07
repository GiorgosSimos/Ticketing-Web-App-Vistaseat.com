package com.unipi.gsimos.vistaseat.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketPDFDto(
        Long ticketId,
        Long bookingId,
        String ticketNumber,
        String barcode,
        String holderFullName,
        String holderEmail,
        String eventName,
        String venueName,
        LocalDateTime eventDateTime,
        BigDecimal ticketPrice,
        String seat

) {
}
