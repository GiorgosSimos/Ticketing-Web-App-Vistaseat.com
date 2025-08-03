package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.BookingInfo;
import com.unipi.gsimos.vistaseat.dto.PendingBookingDto;
import com.unipi.gsimos.vistaseat.model.EventOccurrence;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final EventOccurrenceRepository eventOccurrenceRepository;

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
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/api/events/" + occurrence.getEvent().getId();
        }

    }
}
