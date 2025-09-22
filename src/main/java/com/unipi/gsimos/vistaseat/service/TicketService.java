package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.model.Ticket;

import java.util.List;

public interface TicketService {

    List<Ticket> createTickets(Long bookingId);

    void cancelTickets(Long bookingId);

    byte[] generatePdfTicket(Long ticketId);

    byte[] generatePdfTicketsForBooking(Long bookingId);
}
