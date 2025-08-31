package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.CategoriesEventCardDto;
import com.unipi.gsimos.vistaseat.dto.EventDto;
import com.unipi.gsimos.vistaseat.dto.EventOccurrenceDto;
import com.unipi.gsimos.vistaseat.dto.VenueDto;
import com.unipi.gsimos.vistaseat.mapper.VenueMapper;
import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.BookingService;
import com.unipi.gsimos.vistaseat.service.EventOccurrenceService;
import com.unipi.gsimos.vistaseat.service.EventService;
import com.unipi.gsimos.vistaseat.service.VenueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

//A Web controller (@Controller) works with HTML forms (using @ModelAttribute, returns view names for Thymeleaf).
@Controller
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;
    private final VenueRepository venueRepository;
    private final EventService eventService;
    private final VenueMapper  venueMapper;
    private final EventOccurrenceService eventOccurrenceService;
    private final EventRepository eventRepository;
    private final BookingService bookingService;

    @GetMapping("/adminDashboard/manageVenues")
    public String manageUsers(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String searchQuery,
                              Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Pageable pageable = PageRequest.of(page, size);
        Page<VenueDto> venuesPage;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            venuesPage = venueService.searchVenueByName(searchQuery.trim(), pageable);
        } else {
            venuesPage = venueService.getAllVenues(page, size);
        }

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        model.addAttribute("venues", venuesPage.getContent());

        // .getNumber() is zero-based, +1 is used for display purposes
        model.addAttribute("currentPage", venuesPage.getNumber() + 1);
        model.addAttribute("totalPages", venuesPage.getTotalPages());

        model.addAttribute("searchQuery", searchQuery);

        return "manageVenues";
    }

    @GetMapping("/adminDashboard/manageVenues/addVenue")
    public String addVenue(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        return "addVenue";
    }

    /**
     * Handles POST requests for creating a new venue.
     * <p>
     * Binds form data to a {@link VenueDto}, sets the current authenticated admin as the venue's admin,
     * and attempts to save the new venue using the service layer.
     * <ul>
     *   <li>If creation is successful, a success message is added to flash attributes and the user is redirected to the manage venues page.</li>
     *   <li>If an error occurs, an error message is added to flash attributes and the user is redirected back to the add venue form.</li>
     * </ul>
     * @param venueDto             The data transfer object containing venue details from the form.
     *                             The {@code @ModelAttribute} annotation
     *                             that binds submitted form fields to this object.
     * @param redirectAttributes   Attributes for passing flash messages on redirect.
     * @return Redirect to the appropriate page depending on the operation result.
     */
    @PostMapping("/adminDashboard/manageVenues/addVenue/create")
    public String createVenue(@ModelAttribute VenueDto venueDto,
                              RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        venueDto.setAdminId(user.getId());

        try {
            venueService.createVenue(venueDto);
            redirectAttributes.addFlashAttribute("message", "Venue created successfully");
            return "redirect:/adminDashboard/manageVenues?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: "+e.getMessage());
            return "redirect:/adminDashboard/manageVenues/addVenue";
        }
    }

    @GetMapping("/adminDashboard/manageVenues/editVenue/{venueId}")
    public String showEditVenueForm(@PathVariable Long venueId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("venue", venue);

        return "editVenue";
    }

    // Handle form submission (POST)
    @PostMapping("/adminDashboard/manageVenues/editVenue/{venueId}")
    public String editVenue(@PathVariable Long venueId,
                            @ModelAttribute VenueDto venueDto,
                            RedirectAttributes redirectAttributes) {

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(()-> new RuntimeException("Venue not found"));

        venue.setName(venueDto.getName());
        venue.setStreet(venueDto.getStreet());
        venue.setNumber(venueDto.getNumber());
        venue.setZipcode(venueDto.getZipcode());
        venue.setCity(venueDto.getCity());
        venue.setCapacity(venueDto.getCapacity());

        try {
            venueRepository.save(venue);
            redirectAttributes.addFlashAttribute("message", "Venue updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Venue could not be updated because of an unexpected error: " + e.getMessage());
            return "redirect:/adminDashboard/manageVenues/editVenue/" + venueId;
        }

        return "redirect:/adminDashboard/manageVenues";
    }

    @PostMapping("adminDashboard/manageVenues/delete/{id}")
    public String deleteVenue(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            venueService.deleteVenue(id);
            redirectAttributes.addFlashAttribute("message", "Venue was deleted successfully!");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "Venue not found!");
        }

        return "redirect:/adminDashboard/manageVenues";
    }

    @GetMapping("/adminDashboard/manageVenues/eventsForVenue/{venueId}")
    public String displayEventsForVenue(@PathVariable Long venueId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String searchQuery,
                                        @RequestParam(required = false) String sort,
                                        @RequestParam(defaultValue = "desc") String sortDirection,
                                        Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Venue venue = venueRepository.findById(venueId).
                orElseThrow(() -> new EntityNotFoundException("Venue not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<EventDto> eventsPage;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            eventsPage = eventService.getEventsByNameAndVenue(searchQuery.trim(), venueId, pageable);
        } else {
            eventsPage = eventService.getEventsByVenueId(venueId, pageable);
        }

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("venue", venue);

        // Paging controls
        model.addAttribute("currentPage", eventsPage.getNumber() + 1);
        model.addAttribute("totalPages", eventsPage.getTotalPages());

        List<EventDto> eventsList = new ArrayList<>(eventsPage.getContent());

        // Sort in descending/ascending order by event occurrence count
        if ("occurrenceCount".equals(sort)) {
            if ("asc".equalsIgnoreCase(sortDirection)) {
                eventsList.sort(Comparator.comparingLong(EventDto::getOccurrenceCount));
            } else {
                eventsList.sort(Comparator.comparingLong(EventDto::getOccurrenceCount).reversed());
            }
        }

        model.addAttribute("events", eventsList);
        model.addAttribute("eventsCount", eventsList.size());

        // Sort by number of occurrences
        model.addAttribute("sort", sort);
        model.addAttribute("sortDirection", sortDirection);

        return "venueEvents";
    }

    @GetMapping("/adminDashboard/manageVenues/occurrencesForVenue/{venueId}")
    public String displayVenueOccurrences(@PathVariable Long venueId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                          @RequestParam(required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                          Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new EntityNotFoundException("Venue not found"));

        VenueDto venueDto = venueMapper.toDto(venue);

        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").ascending());
        Page<EventOccurrenceDto> occurrencesPage = eventOccurrenceService.
                getOccurrencesByVenueIdAndDate(venueId, from, to, pageable);
        List<EventOccurrenceDto> occurrencesList = new ArrayList<>(occurrencesPage.getContent());

        long occurrencesCount = occurrencesPage.getTotalElements();

        long totalVenueBookings = bookingService.countBookingsByVenueAndDateBetween(venueId, from, to);

        // TODO Calculate total venue revenue dynamically with date filter

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("venue", venueDto);
        model.addAttribute("occurrences", occurrencesList);
        model.addAttribute("occurrenceCount", occurrencesCount);
        model.addAttribute("totalVenueBookings", totalVenueBookings);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        // Paging controls
        model.addAttribute("currentPage", occurrencesPage.getNumber() + 1);
        model.addAttribute("totalPages", occurrencesPage.getTotalPages());


        return "venueOccurrences";

    }

    @GetMapping("/adminDashboard/manageVenues/occurrencesForVenue/{venueId}/addOccurrence")
    public String addVenueOccurrence(@PathVariable Long venueId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new EntityNotFoundException("Venue not found"));

        VenueDto venueDto = venueMapper.toDto(venue);

        // Pre-load all available events for this venue
        List<Event> events = eventRepository.findAllByVenueId(venueId);
        model.addAttribute("events", events);

        model.addAttribute("venue", venueDto);

        return "addVenueOccurrence";

    }

    @PostMapping("/adminDashboard/manageVenues/occurrencesForVenue/{venueId}/createOccurrence")
    public String createVenueOccurrence(@PathVariable Long venueId,
                                        @ModelAttribute EventOccurrenceDto eventOccurrenceDto,
                                        RedirectAttributes redirectAttributes) {

        Venue occurrenceVenue = venueRepository.findById(venueId)
                .orElseThrow(() -> new EntityNotFoundException("Venue not found"));

        try {
            eventOccurrenceService.createEventOccurrence(eventOccurrenceDto,  occurrenceVenue);
            redirectAttributes.addFlashAttribute("message", "Occurrence created successfully");
            return "redirect:/adminDashboard/manageVenues/occurrencesForVenue/"  + venueId + "?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Occurrence could not be created due to an unexpected error" + e.getMessage());
            return "redirect:/adminDashboard/manageVenues/occurrencesForVenue/" + venueId + "/addOccurrence";
        }
    }

    @GetMapping("/api/venues/{venueId}")
    public String displayEvent(@PathVariable Long venueId,
                               @RequestParam(name = "searchQuery", required = false) String searchQuery,
                               @RequestParam(required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam(required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                               Model model) {


        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new EntityNotFoundException("Venue not found"));

        VenueDto venueDto = venueMapper.toDto(venue);
        String query = (searchQuery == null || searchQuery.trim().isEmpty())
                ? null
                : searchQuery.trim().toLowerCase(Locale.ROOT);

        List<CategoriesEventCardDto> events = eventService.getVenueEvents(venueId, query, from, to);

        model.addAttribute("venue", venueDto);
        model.addAttribute("events", events);
        model.addAttribute("eventsCount", events.size());

        model.addAttribute("searchQuery", query);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "displayVenue";
    }
}
