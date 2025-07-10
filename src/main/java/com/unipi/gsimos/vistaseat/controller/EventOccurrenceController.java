package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.EventDto;
import com.unipi.gsimos.vistaseat.dto.EventOccurrenceDto;
import com.unipi.gsimos.vistaseat.mapper.EventMapper;
import com.unipi.gsimos.vistaseat.mapper.EventOccurrenceMapper;
import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.EventOccurrence;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventOccurrenceRepository;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.EventOccurrenceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventOccurrenceController {

    public final EventRepository eventRepository;
    public final EventMapper eventMapper;
    public final EventOccurrenceMapper  eventOccurrenceMapper;
    public final EventOccurrenceService eventOccurrenceService;
    private final EventOccurrenceRepository eventOccurrenceRepository;
    private final VenueRepository venueRepository;
    private final BookingRepository bookingRepository;

    @GetMapping("/adminDashboard/manageOccurrencesForEvent/{eventId}")
    public String displayOccurrencesForEvent (@PathVariable Long eventId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").ascending());
        Page<EventOccurrenceDto> occurrencesPage = eventOccurrenceService.getOccurrencesByEventId(eventId, pageable);

        List<EventOccurrenceDto> occurrencesListWithBookingCount = new ArrayList<>(occurrencesPage.getContent());
        model.addAttribute("occurrences", occurrencesListWithBookingCount);

        // Paging controls
        model.addAttribute("currentPage", occurrencesPage.getNumber() + 1);
        model.addAttribute("totalPages", occurrencesPage.getTotalPages());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        Long numberOfOccurrences = eventOccurrenceRepository.countEventOccurrenceByEventId(eventId);
        long totalEventBookings = bookingRepository.countBookingsByEventOccurrence_Event_Id(eventId);

        // TODO Calculate total event revenue

        EventDto eventDto = eventMapper.toDto(event);

        model.addAttribute("numberOfEventOccurrences", numberOfOccurrences);
        model.addAttribute("totalEventBookings", totalEventBookings);
        model.addAttribute("event", eventDto);

        return "manageOccurrences";
    }

    @GetMapping("/adminDashboard/manageOccurrencesForEvent/{eventId}/addOccurrence")
    public String addEventOccurrence(@PathVariable Long eventId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        EventDto eventDto = eventMapper.toDto(event);

        model.addAttribute("event", eventDto);
        model.addAttribute("venueCapacity", event.getVenue().getCapacity());

        return "addOccurrence";
    }

    @PostMapping("/adminDashboard/manageOccurrencesForEvent/{eventId}/createOccurrence")
    public String createEventOccurrence(@PathVariable Long eventId,
                                        @ModelAttribute EventOccurrenceDto eventOccurrenceDto,
                                        RedirectAttributes  redirectAttributes) {

        Event eventOfOccurrence = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        Venue eventVenue = venueRepository.findById(eventOfOccurrence.getVenue().getId())
                .orElseThrow(() -> new EntityNotFoundException("Venue not found"));

        eventOccurrenceDto.setEventId(eventId);

        try {
            eventOccurrenceService.createEventOccurrence(eventOccurrenceDto, eventVenue);
            redirectAttributes.addFlashAttribute("message", "Occurrence created successfully");
            return "redirect:/adminDashboard/manageOccurrencesForEvent/{eventId}";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error: "+ex.getMessage());
            return  "redirect:/adminDashboard/manageOccurrencesForEvent/{eventId}/addOccurrence";
        }
    }

    @PostMapping("/adminDashboard/manageOccurrencesForEvent/{eventId}/delete/{occurrenceId}")
    public String deleteEventOccurrence(@PathVariable Long eventId,
                                        @PathVariable Long occurrenceId,
                                        RedirectAttributes redirectAttributes) {

        try {
            eventOccurrenceService.deleteEventOccurrence(occurrenceId);
            redirectAttributes.addFlashAttribute("message", "Occurrence deleted successfully");
            return "redirect:/adminDashboard/manageOccurrencesForEvent/" + eventId + "?success";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error: "+ex.getMessage());
            return "redirect:/adminDashboard/manageOccurrencesForEvent/" +eventId;
        }
    }

    @GetMapping("/adminDashboard/manageOccurrencesForEvent/{eventId}/editOccurrence/{occurrenceId}")
    public String showEditEventOccurrenceForm(@RequestParam(required = false) Long fixedVenueId,
                                              @PathVariable Long eventId,
                                              @PathVariable Long occurrenceId,
                                              Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        EventOccurrence occurrence = eventOccurrenceRepository.findById(occurrenceId)
                .orElseThrow(() -> new EntityNotFoundException("Occurrence not found"));

        EventDto eventDto = eventMapper.toDto(event);
        EventOccurrenceDto occurrenceDto = eventOccurrenceMapper.toDto(occurrence);

        model.addAttribute("event", eventDto);
        model.addAttribute("occurrence", occurrenceDto);
        model.addAttribute("venueCapacity", event.getVenue().getCapacity());

        // Used for editing the occurrences of a specific venue
        if (fixedVenueId != null) {
            model.addAttribute("fixedVenueId", fixedVenueId);
        }

        return "editOccurrence";
    }

    @PostMapping("/adminDashboard/manageOccurrencesForEvent/{eventId}/editOccurrence/{occurrenceId}")
    public String editEventOccurrence(@RequestParam(required = false) Long fixedVenueId,
                                      @PathVariable Long eventId,
                                      @PathVariable Long occurrenceId,
                                      @ModelAttribute EventOccurrenceDto eventOccurrenceDto,
                                      RedirectAttributes redirectAttributes) {

        eventOccurrenceDto.setId(occurrenceId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        Venue eventVenue = venueRepository.findById(event.getVenue().getId())
                .orElseThrow(() -> new EntityNotFoundException("Venue not found"));

        try {
            eventOccurrenceService.updateEventOccurrence(eventOccurrenceDto, eventVenue);
            redirectAttributes.addFlashAttribute("message", "Occurrence updated successfully");
            if (fixedVenueId != null) {
                return "redirect:/adminDashboard/manageVenues/occurrencesForVenue/" + fixedVenueId + "?success";
            }
            return "redirect:/adminDashboard/manageOccurrencesForEvent/" + eventId + "?success";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error",
                    "Occurrence could not be updated because of an unexpected error: "+ex.getMessage());
            if (fixedVenueId != null) {
                return "redirect:/adminDashboard/manageOccurrencesForEvent/" + eventId +
                               "/editOccurrence/" + occurrenceId + "?fixedVenueId="+fixedVenueId;
            }
            return "redirect:/adminDashboard/manageOccurrencesForEvent/" + eventId +
                           "/editOccurrence/" + occurrenceId;
        }
    }
}
