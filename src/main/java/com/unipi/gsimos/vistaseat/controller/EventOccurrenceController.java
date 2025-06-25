package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.EventDto;
import com.unipi.gsimos.vistaseat.dto.EventOccurrenceDto;
import com.unipi.gsimos.vistaseat.mapper.EventMapper;
import com.unipi.gsimos.vistaseat.model.Event;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.service.EventOccurrenceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class EventOccurrenceController {

    public final EventRepository eventRepository;
    public final EventMapper eventMapper;
    public final EventOccurrenceService eventOccurrenceService;

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
        model.addAttribute("venueCapacity", event.getVenue().getCapacity());

        return "addOccurrence";
    }

    @PostMapping("/adminDashboard/manageOccurrencesForEvent/{eventId}/createOccurrence")
    public String createEventOccurrence(@PathVariable Long eventId,
                                        @ModelAttribute EventOccurrenceDto eventOccurrenceDto,
                                        RedirectAttributes  redirectAttributes) {

        eventOccurrenceDto.setEventId(eventId);

        try {
            eventOccurrenceService.createEventOccurrence(eventOccurrenceDto);
            redirectAttributes.addFlashAttribute("message", "Occurrence created successfully");
            return "redirect:/adminDashboard/manageOccurrencesForEvent/{eventId}";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error: "+ex.getMessage());
            return  "redirect:/adminDashboard/manageOccurrencesForEvent/{eventId}/addOccurrence";
        }

    }
}
