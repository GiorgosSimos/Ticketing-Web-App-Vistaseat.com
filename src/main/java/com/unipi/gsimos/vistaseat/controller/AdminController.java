package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.service.UserService;
import com.unipi.gsimos.vistaseat.service.VenueService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.ui.Model;
import com.unipi.gsimos.vistaseat.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final VenueService venueService;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public AdminController(UserRepository userRepository, UserService userService, VenueService venueService, EventRepository eventRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.venueService = venueService;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/adminLogin")
    public String adminLogin(HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            // Redirect to dashboard
            response.sendRedirect("/adminDashboard");
            return null;
        }

        // Otherwise, show the login page
        return "adminLogin";
    }

    @GetMapping("/adminDashboard")
    public String adminDashboard(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        // Admin first and last names
        session.setAttribute("firstName", user.getFirstName());
        session.setAttribute("lastName", user.getLastName());

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        // Total System users count
        model.addAttribute("totalUsers", userService.countAllUsers());

        // Total Venues count
        model.addAttribute("totalVenues", venueService.countVenues());

        // Total Events count
        model.addAttribute("totalEvents",eventRepository.count());

        model.addAttribute("totalBookings", bookingRepository.count());

        // Recent Users
        model.addAttribute("recentUsers", userService.getLast10Users());

        return "adminDashboard";
    }


    @PreAuthorize("hasRole('DOMAIN_ADMIN')")
    @GetMapping("/adminAccount")
    public String adminAccount(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("phone", user.getPhone());

        return "adminAccount";
    }

    @PreAuthorize("hasRole('DOMAIN_ADMIN')")
    @PostMapping("/adminAccount/update")
    public String updateAdminDetails(@RequestParam String firstName,
                                     @RequestParam String lastName,
                                     @RequestParam String phone,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        String email = principal.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No admin found with email: " + email));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);

        try{
            userRepository.save(user);

            // Refresh the user in SecurityContextHolder after update
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user,                      // the updated User object
                    null,                     // credentials -> not providing a password, the user is already authenticated
                    user.getAuthorities());  // the roles/authorities of the user
            SecurityContextHolder.getContext().setAuthentication(authToken);

            redirectAttributes.addFlashAttribute("message",
                    "Your account details were updated successfully!");

        } catch (Exception e){
            redirectAttributes.addFlashAttribute("error",
                    "An unexpected error occurred while updating the account details!" + e.getMessage());
        }

        return "redirect:/adminAccount";
    }
}
