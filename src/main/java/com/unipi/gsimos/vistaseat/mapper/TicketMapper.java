package com.unipi.gsimos.vistaseat.mapper;

import com.unipi.gsimos.vistaseat.dto.TicketDto;
import com.unipi.gsimos.vistaseat.model.Booking;
import com.unipi.gsimos.vistaseat.model.Ticket;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketMapper {

    private final BookingRepository bookingRepository;

    public Ticket toEntity(TicketDto ticketDto) {
        Ticket ticket = new Ticket();

        ticket.setTicketNumber(ticketDto.getTicketNumber());
        ticket.setBarcode(ticketDto.getBarcode());
        ticket.setStatus(ticketDto.getStatus());

        Booking booking = bookingRepository.findById(ticketDto.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        ticket.setBooking(booking);

        return ticket;
    }

    public TicketDto toDto(Ticket ticket) {
        TicketDto ticketDto = new TicketDto();

        ticketDto.setId(ticket.getId());
        ticketDto.setTicketNumber(ticket.getTicketNumber());
        ticketDto.setBarcode(ticket.getBarcode());
        ticketDto.setStatus(ticket.getStatus());
        ticketDto.setBookingId(ticketDto.getBookingId());

        return ticketDto;
    }
}
