package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.VenueDto;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.service.VenueService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//A Web controller (@Controller) works with HTML forms (using @ModelAttribute, returns view names for Thymeleaf).
@Controller
public class VenueController {

    private final VenueService venueService;


    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping("/adminDashboard/manageVenues/addVenue")
    public String addVenue(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        return "addVenue";
    }

    @PostMapping("/adminDashboard/manageVenues/addVenue/create")
    public String createVenue(@ModelAttribute VenueDto venueDto, RedirectAttributes redirectAttributes) {
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
}
