package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.TicketPDFDto;
import com.unipi.gsimos.vistaseat.model.*;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.TicketRepository;
import com.unipi.gsimos.vistaseat.service.TicketService;
import com.unipi.gsimos.vistaseat.utilities.TicketNumberGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final SecureRandom RAND = new SecureRandom();
    private final SpringTemplateEngine templateEngine;

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

    @Override
    public byte[] generatePdfTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket with id: " + ticketId + " not found"));

        Booking booking = ticket.getBooking();
        EventOccurrence occurrence = booking.getEventOccurrence();
        Event event = occurrence.getEvent();
        Venue venue = event.getVenue();

        TicketPDFDto ticketPDFDto = new TicketPDFDto(
                ticketId,
                booking.getId(),
                ticket.getTicketNumber(),
                ticket.getBarcode(),
                booking.getFirstName() + ' ' + booking.getLastName(),
                booking.getEmail(),
                event.getName(),
                venue.getName(),
                occurrence.getEventDate(),
                occurrence.getPrice(),
                "N/A"
        );

        String logoBase64 = encodeImageToBase64("/static/images/logo.png");
        String barcodeBase64 = encodeImageToBase64("/static/images/barcode.png");
        String noticeIconBase64 = encodeImageToBase64("/static/images/notice.png");

        Context context = new Context();
        context.setVariable("logoBase64", logoBase64);
        context.setVariable("barcodeBase64", barcodeBase64);
        context.setVariable("noticeIconBase64", noticeIconBase64);
        context.setVariable("ticket", ticketPDFDto); // must match fields in your template

        String renderedHtml = templateEngine.process("fragments/ticketPDF", context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(renderedHtml);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed",e);
        }
    }

    private String encodeImageToBase64(String pathInResources) {
        try (InputStream stream = getClass().getResourceAsStream(pathInResources)){
            if (stream == null) throw new FileNotFoundException("Resource not found: " + pathInResources);
            byte[] bytes = stream.readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
