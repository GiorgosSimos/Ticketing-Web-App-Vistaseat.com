package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.CategoriesEventCardDto;
import com.unipi.gsimos.vistaseat.dto.EventDto;
import com.unipi.gsimos.vistaseat.mapper.EventMapper;
import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.EventType;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.EventService;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.Map;

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

    @GetMapping("/api/events/{eventId:\\d+}")
    public String displayEvent(@PathVariable Long eventId, Model model) {


        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        model.addAttribute("event", event);

        return "displayEvent";
    }

    /**
     * Handles *all* event‑listing pages that belong to the six supported categories
     * (theater, cinema, concert, sports, museum, archaeological).
     *
     * <p>The incoming URL is constrained by a regular‑expression segment in
     * {@code @GetMapping}, so only the recognised slugs can reach this method:
     *
     * <pre>
     * /api/events/theater
     * /api/events/cinema
     * /api/events/concert
     * /api/events/sports
     * /api/events/museum
     * /api/events/archaeological
     * </pre>
     *
     * <p>Processing steps:</p>
     * <ol>
     *   <li>Convert the lowercase&nbsp;{@code category} slug to an {@link EventType} enum.</li>
     *   <li>Look‑up static UI metadata (title, icon URL, description) from
     *       an in‑memory map.</li>
     *   <li>Fetch all events of that type from {@code eventService}.</li>
     *   <li>Add metadata, event list and count to the {@link Model} so that the
     *       <em>categoryEvents.html</em> view can render them.</li>
     * </ol>
     *
     * @param category the path variable identifying the category
     *                 (must match one of the slugs in the mapping regex)
     * @param model    the model that will be populated for the Thymeleaf view
     * @return the logical view name {@code "categoryEvents"}
     * @throws IllegalArgumentException if the slug cannot be mapped to an {@code EventType}
     */

    @GetMapping("/api/events/{category:theater|cinema|concert|sports|museum|archaeological}")
    private String displayEventsByCategory(@PathVariable("category") String category, Model model) {

        EventType eventType = EventType.valueOf(category.toUpperCase());

        CategoryMeta metadata = META_MAP.get(eventType);

        model.addAttribute("pageTitle",          metadata.title());
        model.addAttribute("categoryIconURL",    metadata.iconURL());
        model.addAttribute("categoryDescription",metadata.description());

        List<CategoriesEventCardDto> events = eventService.getEventsByEventType(eventType);

        model.addAttribute("events", events);
        model.addAttribute("eventsCount", events.size());

        return "categoryEvents";
    }

    /**
     * Immutable lookup table that supplies the UI metadata for each
     * {@link EventType} shown on a category‑listing page.
     *
     * <p>The map key is the enum constant and the value is a {@link CategoryMeta}
     * record that holds:</p>
     * <ul>
     *   <li>a human‑readable page title,</li>
     *   <li>the relative URL of the category icon, and</li>
     *   <li>a short marketing description displayed beneath the heading.</li>
     * </ul>
     *
     * <p>Created via {@code Map.of(…)} at class‑initialisation time, so the
     * collection is unmodifiable and thread‑safe. When a new
     * {@code EventType} is introduced, make sure to add its corresponding
     * entry here; otherwise the controller will lack the metadata needed to
     * render that category’s page.</p>
     */
    private static final Map<EventType, CategoryMeta> META_MAP = Map.of(
            EventType.THEATER, new CategoryMeta(
                    "Theater Plays", "/images/theater_icon.png",
                    "Discover amazing theatrical performances, from ancient tragedies to classical dramas and comedies."),
            EventType.CINEMA, new CategoryMeta(
                    "Cinema Movies", "/images/movie_reel_icon.png",
                    "Discover great movies, from european and world cinema."),
            EventType.CONCERT, new CategoryMeta(
                    "Music Concerts", "/images/music_concert_icon.png",
                    "Enjoy unforgettable live performances, from artists from all around the globe."),
            EventType.SPORTS, new CategoryMeta(
                    "Sport Events", "/images/sports_icon.png",
                    "Football, basketball and more great matches."),
            EventType.MUSEUM, new CategoryMeta(
                    "Museum Visits","/images/museum_icon.png",
                    "Visit wonderful museums, art galleries and exhibitions."),
            EventType.ARCHAEOLOGICAL, new CategoryMeta(
                    "Visit Archaeological Sites", "/images/archaeological_site_icon.png",
                    "Historical, cultural sites and archaeological tours."
            )
    );

    private record CategoryMeta(String title, String iconURL, String description) {}

}
