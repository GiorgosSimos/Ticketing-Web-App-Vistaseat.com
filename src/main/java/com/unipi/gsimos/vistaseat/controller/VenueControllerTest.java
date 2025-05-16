package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.service.VenueService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VenueControllerTest {

    private final VenueService venueService;


    public VenueControllerTest(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping("/adminDashboard/manageUsers/addVenue")
    public String addVenue(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        return "addVenue";
    }
}
