package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.model.*;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.TicketRepository;
import com.unipi.gsimos.vistaseat.service.TicketService;
import com.unipi.gsimos.vistaseat.utilities.TicketNumberGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final SecureRandom RAND = new SecureRandom();

    @Override
    public List<Ticket> createTickets(Long bookingId) {

        Booking confirmedBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking with id: " + bookingId + " not found"));

        int numberOfTickets = confirmedBooking.getNumberOfTickets();
        EventType eventType = confirmedBooking.getEventOccurrence().getEvent().getEventType();

        List<Ticket> tickets = new ArrayList<>();

        for (int i = 0; i < numberOfTickets; i++) {
            tickets.add(createTicket(confirmedBooking, eventType));
        }
        return ticketRepository.saveAll(tickets);
    }

    private Ticket createTicket(Booking booking, EventType eventType) {
        Ticket ticket = new Ticket();
        ticket.setBooking(booking);
        ticket.setStatus(TicketStatus.ACTIVE);
        ticket.setTicketNumber(uniqueTicketNumber(eventType));
        ticket.setBarcode(random10Digits());
        return ticket;
    }

    // A single retry is plenty—collision probability is under 10⁻⁹ per insert.
    private String uniqueTicketNumber(EventType eventType) {
        for (int tries = 0; tries < 2; tries++) {
            String candidate = ticketNumberGenerator.generate(eventType);
            if (!ticketRepository.existsByTicketNumber(candidate)) return candidate;
        }
        throw new IllegalStateException("Could not generate unique ticket number");
    }

    private String random10Digits() {
        long number = RAND.nextLong(10_000_000_000L);
        return String.format("%010d", number);
    }
}
