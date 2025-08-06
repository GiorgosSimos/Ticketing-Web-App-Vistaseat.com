package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {

    private Long id;
    private String ticketNumber;
    private String barcode;
    private TicketStatus status;
    private Long bookingId;
}
