package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.model.EventType;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import org.springframework.ui.Model;
import com.unipi.gsimos.vistaseat.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class EventController {

    private final VenueRepository venueRepository;

    public EventController(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
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
}
