package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.EventDto;
import com.unipi.gsimos.vistaseat.mapper.EventMapper;
import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class EventOccurrenceController {

    public final EventRepository eventRepository;
    public final EventMapper eventMapper;

    @GetMapping("/adminDashboard/manageOccurrencesForEvent/{eventId}")
    public String displayOccurrencesForEvent (@PathVariable Long eventId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        Integer numberOfOccurrences = event.getOccurrences().size();

        EventDto eventDto = eventMapper.toDto(event);

        model.addAttribute("numberOfEventOccurrences", numberOfOccurrences);
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

        return "addOccurrence";
    }
}
