package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.BookingInfo;
import com.unipi.gsimos.vistaseat.dto.EventCardDto;
import com.unipi.gsimos.vistaseat.dto.EventOccurrenceCardDto;
import com.unipi.gsimos.vistaseat.dto.PendingBookingDto;
import com.unipi.gsimos.vistaseat.model.*;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.repository.PaymentRepository;
import com.unipi.gsimos.vistaseat.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final int MIN_TICKETS = 1;
    private static final int MAX_TICKETS = 10;
    private static final BigDecimal FEE_PER_TICKET = new BigDecimal("0.10");// used to calculate the service fee
    private final BookingRepository bookingRepository;
    private final EventOccurrenceRepository eventOccurrenceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public Long countBookingsByVenueAndDateBetween(Long venueId, LocalDate windowStart, LocalDate windowEnd) {

        long bookingCount;

        if (windowEnd == null && windowStart == null) {
            bookingCount = bookingRepository.countByEventOccurrence_Event_Venue_Id(venueId);
        } else if (windowStart == null) {
            bookingCount = bookingRepository.countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateBefore(
                            venueId,
                            windowEnd.plusDays(1).atStartOfDay());
        } else if (windowEnd == null ) {
            bookingCount = bookingRepository.countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateAfter(
                            venueId,
                            windowStart.plusDays(1).atStartOfDay());
        } else {
            bookingCount = bookingRepository.countByEventOccurrence_Event_Venue_IdAndEventOccurrence_EventDateBetween(
                            venueId,
                            windowStart.atStartOfDay(),
                            windowEnd.plusDays(1).atStartOfDay());
        }
        return bookingCount;
    }

    @Override
    public BookingInfo prepareBookingInfo(Long occurrenceId, int requestedTickets) {

        // Check if requested tickets is between MIN_TICKETS and MAX_TICKETS values
        int numberOfTickets = Math.max(MIN_TICKETS, Math.min(requestedTickets, MAX_TICKETS));

        EventOccurrence occurrence = eventOccurrenceRepository.findById(occurrenceId)
                .orElseThrow(() -> new EntityNotFoundException("EventOccurrence not found with id: " + occurrenceId));

        // Calculate service fee and total amount
        BigDecimal serviceFee = FEE_PER_TICKET.multiply(BigDecimal.valueOf(numberOfTickets));
        BigDecimal totalAmount = occurrence.getPrice()
                .multiply(BigDecimal.valueOf(numberOfTickets))
                .add(serviceFee)
                .setScale(2, RoundingMode.HALF_UP);

        // Return helper record to Booking Controller
        return new BookingInfo(
                numberOfTickets,
                serviceFee,
                totalAmount,
                EventOccurrenceCardDto.from(occurrence),
                EventCardDto.from(occurrence.getEvent()),
                occurrence.getEvent().getVenue().getId()
        );
    }

    @Override
    @Transactional
    public Long createBooking(@NotNull PendingBookingDto pendingBookingDto) {

        Booking pendingBooking = new Booking();

        EventOccurrence occurrence = eventOccurrenceRepository.findById(pendingBookingDto.occurrenceId())
                .orElseThrow(() -> new EntityNotFoundException("EventOccurrence not found with id: " + pendingBookingDto.occurrenceId()));

        // TODO: Check availability

        // Calculate service fee and total amount
        BigDecimal serviceFee = FEE_PER_TICKET
                .multiply(BigDecimal.valueOf(pendingBookingDto.numberOfTickets()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal ticketsCost = occurrence.getPrice()
                .multiply(BigDecimal.valueOf(pendingBookingDto.numberOfTickets()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = ticketsCost.add(serviceFee);

        pendingBooking.setEventOccurrence(occurrence);
        //pendingBooking.setUser(); TODO: For logged in users
        pendingBooking.setFirstName(pendingBookingDto.firstName());
        pendingBooking.setLastName(pendingBookingDto.lastName());
        pendingBooking.setEmail(pendingBookingDto.email());
        pendingBooking.setNumberOfTickets(pendingBookingDto.numberOfTickets());
        pendingBooking.setTicketsPrice(ticketsCost);
        pendingBooking.setServiceFee(serviceFee);
        pendingBooking.setTotalAmount(totalAmount);

        bookingRepository.save(pendingBooking);
        return pendingBooking.getId();
    }

    @Override
    @Transactional
    public void confirmBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // Idempotency check
        if (booking.getStatus() == BookingStatus.CONFIRMED) return;

        // Domain validation
        if (booking.getStatus() != BookingStatus.PENDING)
            throw new IllegalStateException("Booking not pending: " + booking.getStatus());

        // Persist payment
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod(PaymentMethods.DEBIT_CREDIT_CARD);
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        // Update Booking
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpiresAt(null);
        bookingRepository.save(booking);
    }
}
