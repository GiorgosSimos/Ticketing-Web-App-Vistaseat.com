package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.model.BookingStatus;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.service.BookingService;
import com.unipi.gsimos.vistaseat.service.UserService;
import com.unipi.gsimos.vistaseat.service.VenueService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import java.time.LocalDate;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final VenueService venueService;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    public AdminController(UserRepository userRepository, UserService userService, VenueService venueService, EventRepository eventRepository, BookingRepository bookingRepository, BookingService bookingService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.venueService = venueService;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
    }

    @GetMapping("/adminLogin")
    public String adminLogin(HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")
            && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
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

        // Total Bookings count
        model.addAttribute("totalBookings", bookingRepository.count());

        // Total Revenue
        model.addAttribute("totalRevenue", bookingRepository
                .getTotalRevenueByStatus(BookingStatus.CONFIRMED));

        // Recent Users
        model.addAttribute("recentUsers", userService.getLast10Users());

        // Recent Bookings
        model.addAttribute("recentBookings", bookingService.getLast10Bookings());

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

    @GetMapping("/adminDashboard/adminGuide")
    public String displayAdminGuide(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("appName", "Vistaseat.com");
        model.addAttribute("supportEmail", "support@vistaseat.com");
        model.addAttribute("lastUpdated", LocalDate.now().toString());

        return "adminGuide";

    }

}
