package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.*;
import com.unipi.gsimos.vistaseat.mapper.BookingMapper;
import com.unipi.gsimos.vistaseat.mapper.PaymentMapper;
import com.unipi.gsimos.vistaseat.model.*;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.repository.PaymentRepository;
import com.unipi.gsimos.vistaseat.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;


@Controller
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final EventOccurrenceRepository eventOccurrenceRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @GetMapping("/adminDashboard/manageBookings")
    public String displayBookings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        return "manageBookings";
    }

    @GetMapping("/api/makeBooking")
    public String makeBooking(@RequestParam Long occurrenceId,
                              @RequestParam int requestedTickets,
                              Model model) {

        // Create helper record to store all the necessary booking info
        BookingInfo info = bookingService.prepareBookingInfo(occurrenceId, requestedTickets);

        model.addAttribute("eventCard", info.eventCard());
        model.addAttribute("venueID", info.venueId());
        model.addAttribute("numberOfTickets", info.numberOfTickets());
        model.addAttribute("serviceFee", info.serviceFee());
        model.addAttribute("totalAmount", info.totalAmount());
        model.addAttribute("occurrenceCard", info.occurrenceCard());

        return "makeBooking";
    }

    @PostMapping("/api/bookings")
    public String createBooking(@ModelAttribute PendingBookingDto pendingBookingDto,
                                RedirectAttributes redirectAttributes) {

        EventOccurrence occurrence = eventOccurrenceRepository.findById(pendingBookingDto.occurrenceId())
                    .orElseThrow(() -> new EntityNotFoundException("Occurrence not found"));

        try {
            Long bookingId = bookingService.createBooking(pendingBookingDto);
            redirectAttributes.addFlashAttribute("bookingId", bookingId);
            return "redirect:/api/payments/"  + bookingId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/api/events/" + occurrence.getEvent().getId();
        }

    }
    @GetMapping("/api/bookingComplete/{bookingId}")
    public String orderCompleteOverview(@PathVariable Long bookingId, Model model) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
        BookingDto bookingDto = bookingMapper.toDto(booking);

        EventOccurrence occurrence = eventOccurrenceRepository.findById(booking.getEventOccurrence().getId())
                .orElseThrow(() -> new EntityNotFoundException("Occurrence not found with id: "
                        + booking.getEventOccurrence().getId()));

        Event event = occurrence.getEvent();
        CategoriesEventCardDto eventCardDto = CategoriesEventCardDto.from(event);

        Payment payment = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.COMPLETED);
        PaymentDto paymentDto = payment !=null ? paymentMapper.toDto(payment) : null;
        Long paymentId = paymentDto != null ? paymentDto.getId() : null;
        PaymentMethods paymentMethod = paymentDto != null ? paymentDto.getPaymentMethod() : null;
        LocalDateTime paymentDate = paymentDto != null ? paymentDto.getPaymentDateTime() : null;

        model.addAttribute("booking", bookingDto);
        model.addAttribute("CONFIRMED", BookingStatus.CONFIRMED);
        model.addAttribute("PENDING", BookingStatus.PENDING);
        model.addAttribute("CANCELLED", BookingStatus.CANCELLED);
        model.addAttribute("eventCard", eventCardDto);
        model.addAttribute("transactionID", paymentId);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentDate", paymentDate);
        model.addAttribute("occurrenceDateTime", occurrence.getEventDate());
        model.addAttribute("occurrenceDuration", occurrence.getDuration());

        return "bookingOrderComplete";
    }
}
