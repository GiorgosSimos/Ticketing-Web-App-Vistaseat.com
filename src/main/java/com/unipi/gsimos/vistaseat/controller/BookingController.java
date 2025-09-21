package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.*;
import com.unipi.gsimos.vistaseat.mapper.BookingMapper;
import com.unipi.gsimos.vistaseat.mapper.PaymentMapper;
import com.unipi.gsimos.vistaseat.mapper.TicketMapper;
import com.unipi.gsimos.vistaseat.mapper.UserMapper;
import com.unipi.gsimos.vistaseat.model.*;
import com.unipi.gsimos.vistaseat.repository.*;
import com.unipi.gsimos.vistaseat.service.BookingService;
import com.unipi.gsimos.vistaseat.service.EventOccurrenceService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final EventOccurrenceRepository eventOccurrenceRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final UserRepository userRepository;
    private final EventOccurrenceService eventOccurrenceService;

    @GetMapping("/adminDashboard/manageBookings")
    public String displayBookings(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) SearchField searchField,
                                  @RequestParam(required = false) String searchQuery,
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                  @RequestParam(required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                  Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        Pageable pageable = PageRequest.of(page, size,Sort.by("bookingDate").descending());
        Page<BookingDto> bookingsPage;

        if (searchField == SearchField.EVENT_NAME) {
            bookingsPage = bookingService.getAllBookingsByEventName(searchQuery, pageable);
        } else if (searchField == SearchField.BOOKING_ID) {
            Long bookingId = parseLongOrNull(searchQuery);
            if (bookingId == null) {
                bookingsPage = bookingService.getAllBookings(pageable);
            } else {
                bookingsPage = bookingService.getBookingById(bookingId, pageable);
            }
        } else if (searchField == SearchField.CUSTOMER_EMAIL) {
            bookingsPage = bookingService.getAllBookingsByEmail(searchQuery, pageable);
        } else if (searchField == SearchField.VENUE_NAME) {
            bookingsPage = bookingService.getAllBookingsByVenueName(searchQuery, pageable);
        } else if (from != null && to != null) {
            bookingsPage = bookingService.getBookingsByEventDateRange(from, to, pageable);
        } else {
            bookingsPage = bookingService.getAllBookings(pageable);
        }

        List<BookingDto> bookingsList = new ArrayList<>(bookingsPage.getContent());
        model.addAttribute("bookingsList", bookingsList);

        // Paging controls
        model.addAttribute("currentPage", bookingsPage.getNumber() + 1);
        model.addAttribute("totalPages", bookingsPage.getTotalPages());

        // Date Picker parameters
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "manageBookings";
    }

    @GetMapping("/adminDashboard/manageBookings/viewBooking/{bookingId}")
    public String showEditBookingForm(@PathVariable Long bookingId,
                                      Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking with id: " + bookingId + " not found."));
        BookingDto bookingDto = BookingMapper.toDto(booking);

        EventOccurrence occurrence = eventOccurrenceRepository.findById(booking.getEventOccurrence().getId())
                .orElseThrow(() -> new EntityNotFoundException("Occurrence not found with id: "
                        + booking.getEventOccurrence().getId()));

        Event event = booking.getEventOccurrence().getEvent();

        CategoriesEventCardDto eventCardDto = CategoriesEventCardDto.from(event);

        List<EventOccurrenceCardDto> occurrenceCards = eventOccurrenceService.getOccurrencesByEventId(event.getId());

        Payment payment = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.COMPLETED);
        PaymentDto paymentDto = payment != null ? paymentMapper.toDto(payment) : null;
        Long paymentId = paymentDto != null ? paymentDto.getId() : null;
        PaymentMethods paymentMethod = paymentDto != null ? paymentDto.getPaymentMethod() : null;
        LocalDateTime paymentDate = paymentDto != null ? paymentDto.getPaymentDateTime() : null;

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("booking", bookingDto);
        model.addAttribute("CONFIRMED", BookingStatus.CONFIRMED);
        model.addAttribute("CANCELLED", BookingStatus.CANCELLED);
        model.addAttribute("SOLD_OUT", EventOccurrenceCardDto.AvailabilityLevel.SOLD_OUT);
        model.addAttribute("eventCard", eventCardDto);
        model.addAttribute("CURRENT_DATETIME", LocalDateTime.now());
        model.addAttribute("occurrenceCards", occurrenceCards);
        model.addAttribute("occurrenceCount", occurrenceCards.size());
        model.addAttribute("occurrenceDateTime", occurrence.getEventDate());
        model.addAttribute("occurrenceDuration", occurrence.getDuration());
        model.addAttribute("transactionID", paymentId);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentDate", paymentDate);

        return "editBooking";

    }

    @PostMapping("/adminDashboard/manageBookings/viewBooking/{bookingId}/reschedule")
    public String rescheduleBooking(@PathVariable Long bookingId,
                                    @RequestParam("newOccurrenceId") Long newOccurrenceId,
                                    RedirectAttributes redirectAttributes) {

        try {
            bookingService.rescheduleBooking(bookingId, newOccurrenceId);
            redirectAttributes.addFlashAttribute("message", "Booking successfully rescheduled.");
            return "redirect:/adminDashboard/manageBookings/viewBooking/"+bookingId+"?rescheduled=success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An  error occurred while rescheduling the booking.");
            return "redirect:/adminDashboard/manageBookings/viewBooking/";
        }

    }

    @PostMapping("/adminDashboard/manageBookings/viewBooking/{bookingId}/cancel-refund")
    public String cancelBookingAndRefund(@PathVariable Long bookingId,
                                         RedirectAttributes redirectAttributes) {

        try {
            bookingService.cancelBookingAndRefund(bookingId);
            redirectAttributes.addFlashAttribute("message", "Booking successfully cancelled.");
            return "redirect:/adminDashboard/manageBookings/viewBooking/"+bookingId+"?cancelled=success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An  error occurred while canceling the booking.");
            return "redirect:/adminDashboard/manageBookings/viewBooking/"+bookingId;
        }

    }

    @PostMapping("/adminDashboard/manageBookings/delete/{bookingId}")
    public String deleteBooking(@PathVariable Long bookingId,
                                RedirectAttributes redirectAttributes) {

        try {
            bookingService.deleteBooking(bookingId);
            redirectAttributes.addFlashAttribute("message", "Booking deleted successfully.");
            return "redirect:/adminDashboard/manageBookings?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return  "redirect:/adminDashboard/manageBookings";
        }

    }

    @GetMapping("/api/makeBooking")
    public String makeBooking(@RequestParam Long occurrenceId,
                              @RequestParam int requestedTickets,
                              @RequestParam(required = false, defaultValue = "false") boolean continueAsGuest,
                              Model model,
                              HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // A user is logged-in if we have an auth object, it’s marked authenticated,
        // and it’s NOT the anonymous token/principal.
        boolean isLoggedIn =
                           auth != null
                        && auth.isAuthenticated()
                        && !(auth instanceof AnonymousAuthenticationToken)
                        && (auth.getPrincipal() instanceof UserDetails);

        // If the user is not logged and hasn't opted to continue as guest
        if (!isLoggedIn && !continueAsGuest) {
            // Build the URL of the current page to use it for redirect after successful log in
            String currentUrl = request.getRequestURL()
                    + (request.getQueryString() == null ? "" : "?" + request.getQueryString());

            return "redirect:/loginRequest?redirectTo=" + UriUtils.encode(currentUrl, StandardCharsets.UTF_8);
        }

        UserDto userDto = null;

        if (isLoggedIn) {
            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElseThrow(() ->
                    new EntityNotFoundException("User with email:" + email + " not found"));
            userDto = UserMapper.toUserDto(user);
        }

        // Create helper record to store all the necessary booking info
        BookingInfo info = bookingService.prepareBookingInfo(occurrenceId, requestedTickets);

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("user", userDto);
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

        model.addAttribute("booking", bookingDto);
        model.addAttribute("tickets", tickets);
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

    @PreAuthorize("hasRole('REGISTERED')")
    @GetMapping("/userAccount/myOrders")
    public String myOrders(@RequestParam(name = "tab", defaultValue = "active") String tab,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "5") int size,
                           Model model) {

        Authentication  auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email: " + email + " not found"));

        Pageable pageableActive = PageRequest.of(page, size, Sort.by("eventOccurrence.eventDate").ascending());
        Page<OrderCardDto> activeOrders = bookingService.getActiveOrdersByUserId(user.getId(), pageableActive);

        List<OrderCardDto> activeOrdersList = new ArrayList<>(activeOrders.getContent());
        model.addAttribute("activeOrdersList", activeOrdersList);
        model.addAttribute("activeOrdersCount", activeOrders.getTotalElements());

        Pageable pageablePast = PageRequest.of(page, size, Sort.by("eventOccurrence.eventDate").descending());
        Page<OrderCardDto> pastOrders = bookingService.getPastOrdersByUserId(user.getId(), pageablePast);

        List<OrderCardDto> pastOrdersList = new ArrayList<>(pastOrders.getContent());
        model.addAttribute("pastOrdersList", pastOrdersList);
        model.addAttribute("pastOrdersCount", pastOrders.getTotalElements());

        // Paging controls for active orders
        model.addAttribute("activeCurrentPage", activeOrders.getNumber() + 1);
        model.addAttribute("activeTotalPages", activeOrders.getTotalPages());

        // Paging controls for past orders
        model.addAttribute("pastCurrentPage", pastOrders.getNumber() + 1);
        model.addAttribute("pastTotalPages", pastOrders.getTotalPages());

        model.addAttribute("tab", tab);

        return "myOrders";
    }

    private Long parseLongOrNull(String s) {
        if (s == null) return null;
        try {
            return Long.valueOf(s.trim());
        }  catch (NumberFormatException e) {
            return null;
        }
    }

}