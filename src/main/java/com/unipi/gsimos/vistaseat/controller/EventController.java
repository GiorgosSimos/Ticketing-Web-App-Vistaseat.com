package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.EventDto;
import com.unipi.gsimos.vistaseat.model.EventType;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.EventService;
import org.springframework.ui.Model;
import com.unipi.gsimos.vistaseat.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class EventController {

    private final VenueRepository venueRepository;
    private final EventService eventService;

    public EventController(VenueRepository venueRepository, EventService eventService) {
        this.venueRepository = venueRepository;
        this.eventService = eventService;
    }

    @GetMapping("/adminDashboard/manageEvents")
    public String manageEvents(@RequestParam(required = false) EventType eventType, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        //This keeps track of the current filter so the right button can be highlighted or pre-selected in the UI.
        model.addAttribute("selectEventType", eventType);

        return "manageEvents";

    }

    @GetMapping("/adminDashboard/manageEvents/addEvent")
    public String addEvent(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        // Pre-load all available event types
        model.addAttribute("eventTypes", EventType.values());

        // Pre-load all available venues
        List<Venue> venues = venueRepository.findAll();
        model.addAttribute("venues", venues);

        return "addEvent";
    }

    @PostMapping("/adminDashboard/manageEvents/addEvent/create")
    public String createVenue(@ModelAttribute EventDto eventDto,
                              RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        eventDto.setAdminId(user.getId());

        try {
            eventService.createEvent(eventDto);
            redirectAttributes.addFlashAttribute("message", "Event created successfully");
            return "redirect:/adminDashboard/manageEvents?success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: "+e.getMessage());
            return "redirect:/adminDashboard/manageEvents/addEvent";
        }
    }
}
