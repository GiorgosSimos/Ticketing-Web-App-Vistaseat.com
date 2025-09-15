package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.*;
import com.unipi.gsimos.vistaseat.mapper.BookingMapper;
import com.unipi.gsimos.vistaseat.model.*;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.repository.PaymentRepository;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final int MIN_TICKETS = 1;
    private static final int MAX_TICKETS = 10;
    private static final BigDecimal FEE_PER_TICKET = new BigDecimal("0.10");// used to calculate the service fee
    private final BookingRepository bookingRepository;
    private final EventOccurrenceRepository eventOccurrenceRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

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
    public Page<OrderCardDto> getActiveOrdersByUserId(Long userId, Pageable pageable) {
        return bookingRepository.findActiveBookingsByUserId(userId,pageable)
                .map(OrderCardDto::from);
    }

    @Override
    public Page<OrderCardDto> getPastOrdersByUserId(Long userId, Pageable pageable) {
        return bookingRepository.findPastBookingsByUserId(userId, pageable)
                .map(OrderCardDto::from);
    }

    @Override
    public List<BookingDto> getLast10Bookings() {
        List<Booking> bookings = bookingRepository.findTop10ByOrderByBookingDateDesc();
            return bookings.stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
    }

    @Override
    public Page<BookingDto> getAllBookings(Pageable pageable) {

        Page<Booking> bookings = bookingRepository.findAll(pageable);
        return bookings.map(BookingMapper::toDto);
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

        // Check if event is sold out or there are not enough remaining seats
        if (occurrence.isSoldOut()){
            throw new RuntimeException("We're sorry, the event is sold out");
        } else if (occurrence.getRemainingSeats() < pendingBookingDto.numberOfTickets()){
            throw new RuntimeException("What an inconvenience!!! The remaining seats for this event are " + occurrence.getRemainingSeats());
        }

        // Calculate service fee and total amount
        BigDecimal serviceFee = FEE_PER_TICKET
                .multiply(BigDecimal.valueOf(pendingBookingDto.numberOfTickets()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal ticketsCost = occurrence.getPrice()
                .multiply(BigDecimal.valueOf(pendingBookingDto.numberOfTickets()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = ticketsCost.add(serviceFee);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn =
                auth != null
                        && auth.isAuthenticated()
                        && !(auth instanceof AnonymousAuthenticationToken)
                        && (auth.getPrincipal() instanceof UserDetails);

        pendingBooking.setEventOccurrence(occurrence);
        if (isLoggedIn) {
            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new EntityNotFoundException("User with email: " + email + " not found"));
            pendingBooking.setUser(user);
        }
        pendingBooking.setFirstName(pendingBookingDto.firstName());
        pendingBooking.setLastName(pendingBookingDto.lastName());
        pendingBooking.setPhoneNumber(pendingBookingDto.phoneNumber());
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
    public void confirmBooking(Long bookingId, PaymentMethods paymentMethod) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // Booking has expired - should not happen because of scheduled cancelExpiredBookings() check
        if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            return;
        }

        // Idempotency check - booking is already confirmed
        if (booking.getStatus() == BookingStatus.CONFIRMED) return;

        // Domain validation
        if (booking.getStatus() != BookingStatus.PENDING)
            throw new IllegalStateException("Booking not pending: " + booking.getStatus());

        // Persist payment
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        // Update Booking
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpiresAt(null);
        bookingRepository.save(booking);
    }

    /**
     * Runs once every minute (scheduler thread) and flips all
     * <code>PENDING</code> bookings whose <em>expiresAt</em> timestamp is
     * earlier than <code>LocalDateTime.now()</code> to <code>EXPIRED</code>.
     * <p>
     * The method executes inside a single database transaction and logs
     * how many rows were updated and how long the batch took.
     *
     * <ul>
     *   <li><b>Schedule :</b> {@literal @Scheduled(fixedRate = 60 000)}
     *       ⇒ invoked ~60 000 ms after the <em>start</em> of the previous run.</li>
     *   <li><b>Performance :</b> relies on
     *       {@code BookingRepository#expireOlderThan(LocalDateTime)} which
     *       should be backed by an index on {@code (status, expires_at)}.</li>
     *   <li><b>Threading :</b> executed by Spring’s scheduler pool, never by
     *       a request-handling thread.</li>
     * </ul>
     */
    @Scheduled(fixedRate = 60000)          // 60 000 ms = 1 min
    @Transactional
    public void cancelExpiredBookings() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int rows = bookingRepository.expireOlderThan(LocalDateTime.now());
        stopWatch.stop();
        log.info("Expired {} bookings (took {} ms)", rows, stopWatch.getTotalTimeMillis());
    }
}
