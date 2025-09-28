package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.BookingDto;
import com.unipi.gsimos.vistaseat.dto.CategoriesEventCardDto;
import com.unipi.gsimos.vistaseat.dto.PaymentDto;
import com.unipi.gsimos.vistaseat.dto.TicketDto;
import com.unipi.gsimos.vistaseat.mapper.BookingMapper;
import com.unipi.gsimos.vistaseat.mapper.PaymentMapper;
import com.unipi.gsimos.vistaseat.mapper.TicketMapper;
import com.unipi.gsimos.vistaseat.model.*;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.repository.PaymentRepository;
import com.unipi.gsimos.vistaseat.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TicketController {

    private final BookingRepository bookingRepository;
    private final EventOccurrenceRepository eventOccurrenceRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    @GetMapping("/adminDashboard/manageBookings/{bookingId}/viewTickets")
    public String viewTickets (@PathVariable Long bookingId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
        BookingDto bookingDto = BookingMapper.toDto(booking);

        EventOccurrence occurrence = eventOccurrenceRepository.findById(booking.getEventOccurrence().getId())
                .orElseThrow(() -> new EntityNotFoundException("Occurrence not found with id: "
                        + booking.getEventOccurrence().getId()));

        Event event = occurrence.getEvent();

        CategoriesEventCardDto eventCardDto = CategoriesEventCardDto.from(event);

        Payment payment = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.COMPLETED);
        PaymentDto paymentDto = payment != null ? paymentMapper.toDto(payment) : null;
        Long paymentId = paymentDto != null ? paymentDto.getId() : null;
        PaymentMethods paymentMethod = paymentDto != null ? paymentDto.getPaymentMethod() : null;
        LocalDateTime paymentDate = paymentDto != null ? paymentDto.getPaymentDateTime() : null;

        List<TicketDto> tickets = ticketRepository.
                findAllByBookingId(bookingId).stream().map(ticketMapper::toDto).toList();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("booking", bookingDto);
        model.addAttribute("tickets", tickets);
        model.addAttribute("CONFIRMED", BookingStatus.CONFIRMED);
        model.addAttribute("PENDING", BookingStatus.PENDING);
        model.addAttribute("CANCELLED", BookingStatus.CANCELLED);
        model.addAttribute("REFUNDED", BookingStatus.REFUNDED);
        model.addAttribute("eventCard", eventCardDto);
        model.addAttribute("transactionID", paymentId);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentDate", paymentDate);
        model.addAttribute("occurrenceDateTime", occurrence.getEventDate());
        model.addAttribute("occurrenceDuration", occurrence.getDuration());

        return "viewTickets";

    }
}
