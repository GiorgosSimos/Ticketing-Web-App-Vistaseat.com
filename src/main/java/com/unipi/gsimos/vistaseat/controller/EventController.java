package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.EventDto;
import com.unipi.gsimos.vistaseat.mapper.EventMapper;
import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.EventType;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import com.unipi.gsimos.vistaseat.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventController {

    private final VenueRepository venueRepository;
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @GetMapping("/adminDashboard/manageEvents")
    public String manageEvents(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) EventType eventType,
                               @RequestParam(required = false) String searchQuery,
                               @RequestParam(required = false) String sort,
                               @RequestParam(defaultValue = "desc") String sortDirection,
                               Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        Pageable pageable = PageRequest.of(page, size);
        Page<EventDto> eventsPage;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            eventsPage = eventService.getEventsByName(searchQuery.trim(), pageable);
        } else if (eventType != null) {
            eventsPage = eventService.getEventsByEventType(eventType, page, size);
        } else {
            eventsPage = eventService.getAllEvents(page, size);
        }

        List<EventDto> eventsList = new ArrayList<>(eventsPage.getContent());
        model.addAttribute("events", eventsList);

        // Paging controls
        model.addAttribute("currentPage", eventsPage.getNumber() + 1);
        model.addAttribute("totalPages", eventsPage.getTotalPages());

        // Sort in descending/ascending order by event occurrence count
        if ("occurrenceCount".equals(sort)) {
            if ("asc".equalsIgnoreCase(sortDirection)) {
                eventsList.sort(Comparator.comparingLong(EventDto::getOccurrenceCount));
            } else {
                eventsList.sort(Comparator.comparingLong(EventDto::getOccurrenceCount).reversed());
            }
        }

        // Sort by number of occurrences
        model.addAttribute("sort", sort);
        model.addAttribute("sortDirection", sortDirection);

        // This keeps track of the current event type filter
        // so the right button can be highlighted or pre-selected in the UI.
        model.addAttribute("selectEventType", eventType);

        return "manageEvents";

    }

    @GetMapping("/adminDashboard/manageEvents/addEvent")
    public String addEvent(@RequestParam(required = false) Long fixedVenueId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        // Pre-load all available event types
        model.addAttribute("eventTypes", EventType.values());

        // Pre-load all available venues
        List<Venue> venues = venueRepository.findAll();
        model.addAttribute("venues", venues);

        // used for adding events to a specific venue
        if (fixedVenueId != null) {
            model.addAttribute("fixedVenueId", fixedVenueId);
            model.addAttribute("fixedVenueName",
                    venues.stream().filter(v-> v.getId().equals(fixedVenueId))
                            .findFirst().map(Venue::getName).orElse(""));
        }

        return "addEvent";
    }

    @PostMapping("/adminDashboard/manageEvents/addEvent/create")
    public String createVenue(@RequestParam(required = false) Long fixedVenueId,
                              @ModelAttribute EventDto eventDto,
                              RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        eventDto.setAdminId(user.getId());

        try {
            eventService.createEvent(eventDto);
            redirectAttributes.addFlashAttribute("message", "Event created successfully");
            if (fixedVenueId != null) {
                return "redirect:/adminDashboard/manageVenues/eventsForVenue/" + fixedVenueId + "?success";
            }
            return "redirect:/adminDashboard/manageEvents?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: "+e.getMessage());
            if (fixedVenueId != null) {
                return "redirect:/adminDashboard/manageVenues/eventsForVenue/" + fixedVenueId;
            }

            return "redirect:/adminDashboard/manageEvents/addEvent";
        }
    }

    @GetMapping("/adminDashboard/manageEvents/editEvent/{eventId}")
    public String showEditEventForm(@RequestParam(required = false) Long fixedVenueId,
                                    @PathVariable Long eventId,
                                    Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Event eventEntity = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        EventDto event = eventMapper.toDto(eventEntity);

        List<Venue> venues = venueRepository.findAll();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("event", event);
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("venues", venues);

        // used for editing events for a specific venue
        if (fixedVenueId != null) {
            model.addAttribute("fixedVenueId", fixedVenueId);
            model.addAttribute("fixedVenueName",
                    venues.stream().filter(v-> v.getId().equals(fixedVenueId))
                            .findFirst().map(Venue::getName).orElse(""));
        }

        return "editEvent";
    }

    @PostMapping("/adminDashboard/manageEvents/editEvent/{eventId}")
    public String editEvent(@RequestParam(required = false) Long fixedVenueId,
                            @PathVariable Long eventId,
                            @ModelAttribute EventDto eventDto,
                            RedirectAttributes redirectAttributes) {

        // Find the current event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Find the updated venue
        Venue venue = venueRepository.findById(eventDto.getVenueId())
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        // Update the current event
        event.setName(eventDto.getName());
        event.setEventType(eventDto.getEventType());
        event.setVenue(venue);// Pass the updated venue
        event.setDescription(eventDto.getDescription());

        try {
            eventRepository.save(event);
            redirectAttributes.addFlashAttribute("message", "Event updated successfully");
            if (fixedVenueId != null) {
                return "redirect:/adminDashboard/manageVenues/eventsForVenue/" + fixedVenueId + "?success";
            }
            return "redirect:/adminDashboard/manageEvents?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Event could not be updated because of an unexpected error: "+e.getMessage());
            if (fixedVenueId != null) {
                return "redirect:/adminDashboard/manageVenues/eventsForVenue/" + fixedVenueId;
            }
            return "redirect:/adminDashboard/manageEvents/editEvent/{eventId}";
        }
    }

    @PostMapping("/adminDashboard/manageEvents/delete/{eventId}")
    public String deleteEvent(@PathVariable Long eventId, RedirectAttributes redirectAttributes) {

        try {
            eventService.deleteEvent(eventId);
            redirectAttributes.addFlashAttribute("message", "Event deleted successfully");
            return "redirect:/adminDashboard/manageEvents?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: "+e.getMessage());
            return "redirect:/adminDashboard/manageEvents";
        }



    }
}
