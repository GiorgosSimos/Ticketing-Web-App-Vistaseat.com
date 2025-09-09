package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.*;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.UserRole;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.BookingService;
import com.unipi.gsimos.vistaseat.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;

    @GetMapping("/home")
    public String home(Model model) {

        // Fill Browse by category section
        List<CategoryCardDto> categories = List.of(
                new CategoryCardDto("Theater", "/images/theater_play.jpg",
                        "Ancient tragedies, comedies, plays and all kinds of theatrical shows and performances",
                        "100+ shows", "/api/events/theater"),
                new CategoryCardDto("Cinema", "/images/feature_film.jpg",
                        "European and world cinema. Latest, classic movies and cinephile screenings",
                        "300+ movies", "/api/events/cinema"),
                new CategoryCardDto("Music Concerts", "/images/music_concert.jpg",
                        "Live concerts, festivals and musical events",
                        "300+ concerts", "/api/events/concert"),
                new CategoryCardDto("Sports", "/images/sports.jpg",
                        "Football, basketball, tennis and more",
                        "100+ events", "/api/events/sports"),
                new CategoryCardDto("Museums", "/images/museum_visit.png",
                        "Art galleries, exhibitions and cultural sites",
                        "80+ museums", "/api/events/museum"),
                new CategoryCardDto("Archaeological Sites", "/images/archaeological_site.jpg",
                        "Historical Sites and archaeological tours",
                        "30+ sites", "/api/events/archaeological")
        );
        model.addAttribute("categories", categories);

        // Fill event slider in featured events section
        List<EventCardDto> eventCards = eventRepository.findUpcomingWithAtLeastOneOccurrence()
                .stream().map(EventCardDto::from)
                .toList();
        model.addAttribute("eventCards", eventCards);

        // Fill venue slider in featured venues section
        List<VenueCardDto> venueCards = venueRepository.findAll()
                .stream().map(VenueCardDto::from)
                .toList();

        model.addAttribute("venueCards", venueCards);

        return "index";
    }

    @GetMapping("/userLogin")
    public String userLogin() {return "userLogin";}

    @GetMapping("/loginRequest")
    public String loginRequest(@RequestParam String redirectTo, Model model) {

        model.addAttribute("redirectTo", redirectTo);
        return "loginRequest";
    }

    @GetMapping("/userSignUp")
    public String userSignup() {return "userSignUp";}

    @PreAuthorize("hasRole('REGISTERED')")
    @GetMapping("/userAccount/details")
    public String userAccountDetails(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).
                orElseThrow(() -> new EntityNotFoundException("User with email: " + email + " not found"));

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("phone", user.getPhone());

        return "userAccountDetails";
    }

    @PreAuthorize("hasRole('REGISTERED')")
    @PostMapping("/userAccount/update")
    public String updateUserDetails(@RequestParam String firstName,
                                    @RequestParam String lastName,
                                    @RequestParam String phone,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        String email = principal.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with email: " + email));

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

        return "redirect:/userAccount/details";
    }

    @PreAuthorize("hasRole('REGISTERED')")
    @GetMapping("/userAccount/myOrders")
    public String myOrders(@RequestParam(name = "tab", defaultValue = "active") String tab,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "5") int size,
                           Model model) {

        Authentication  auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email: " + email + " not found"));

        Pageable pageableActive = PageRequest.of(page, size, Sort.by("eventOccurrence.eventDate").ascending());
        Page<OrderCardDto> activeOrders = bookingService.getActiveOrdersByUserId(user.getId(), pageableActive);

        List<OrderCardDto> activeOrdersList = new ArrayList<>(activeOrders.getContent());
        model.addAttribute("activeOrdersList", activeOrdersList);
        model.addAttribute("activeOrdersCount", activeOrders.getTotalElements());

        Pageable pageablePast = PageRequest.of(page, size, Sort.by("eventOccurrence.eventDate").descending());
        Page<OrderCardDto> pastOrders = bookingService.getPastOrdersByUserId(user.getId(), pageablePast);

        List<OrderCardDto> pastOrdersList = new ArrayList<>(pastOrders.getContent());
        model.addAttribute("pastOrdersList", pastOrdersList);
        model.addAttribute("pastOrdersCount", pastOrders.getTotalElements());

        // Paging controls for active orders
        model.addAttribute("activeCurrentPage", activeOrders.getNumber() + 1);
        model.addAttribute("activeTotalPages", activeOrders.getTotalPages());

        // Paging controls for past orders
        model.addAttribute("pastCurrentPage", pastOrders.getNumber() + 1);
        model.addAttribute("pastTotalPages", pastOrders.getTotalPages());

        model.addAttribute("tab", tab);

        return "myOrders";
    }

    @PreAuthorize("hasRole('REGISTERED')")
    @GetMapping("/userAccount/testimonials")
    public String writeTestimonial(Model model) {

        return "writeTestimonial";
    }

    /**
     * Handles GET requests to the admin dashboard's Manage Users page.
     * <p>
     * This method retrieves a paginated list of users from the database, either all users or
     * filtered by a specified role (e.g., ADMIN, USER).
     * It also extracts the currently
     * authenticated admin's name for display and prepares pagination and filter data for the view.
     *
     * @param page the zero-based index of the current page (default is 0)
     * @param size the number of users per page (default is 10)
     * @param role optional filter to return only users with a specific role
     * @param searchQuery optional search filter to return only users with a specific name
     * @param model the Spring Model used to pass data to the Thymeleaf view
     * @return the name of the Thymeleaf template to render ("manageUsers")
     */
    @GetMapping("/adminDashboard/manageUsers")
    public String manageUsers(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) UserRole role,
                              @RequestParam(required = false) String searchQuery,
                              Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserDto> usersPage;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            usersPage = userService.searchUsersByName(searchQuery.trim(), pageable);
        } else if (role != null) {
            usersPage = userService.getUsersByRole(role, page, size);
        } else {
            usersPage = userService.getAllUsers(page, size);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("users", usersPage.getContent());

        // User counts
        model.addAttribute("totalUsers", userService.countAllUsers());
        model.addAttribute("registered", userService.countUsersByRole(UserRole.REGISTERED));
        model.addAttribute("admins", userService.countUsersByRole(UserRole.DOMAIN_ADMIN));

        // .getNumber() is zero-based, +1 is used for display purposes
        model.addAttribute("currentPage", usersPage.getNumber() + 1);
        model.addAttribute("totalPages", usersPage.getTotalPages());

        //This keeps track of the current filter so the right button can be highlighted or pre-selected in the UI.
        model.addAttribute("selectRole", role);

        model.addAttribute("searchQuery", searchQuery);

        return "manageUsers";
    }

    @PostMapping("/adminDashboard/manageUsers/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("message", "User status changed successfully!");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        }

        return "redirect:/adminDashboard/manageUsers";
    }

    @PostMapping("adminDashboard/manageUsers/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "User was deleted successfully!");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminDashboard/manageUsers";
    }

}
