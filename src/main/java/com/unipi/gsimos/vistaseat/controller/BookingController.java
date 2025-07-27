package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.BookingInfo;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private static final BigDecimal SERVICE_FEE_PER_TICKET = BigDecimal.valueOf(0.10);
    private static final int MIN_TICKETS = 1;
    private static final int MAX_TICKETS = 10;
    private final EventRepository eventRepository;
    private final EventOccurrenceRepository eventOccurrenceRepository;
    private final BookingService bookingService;

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
}
