package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class BookingController {

    @GetMapping("/adminDashboard/manageBookings")
    public String displayBookings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        return "manageBookings";
    }
}
