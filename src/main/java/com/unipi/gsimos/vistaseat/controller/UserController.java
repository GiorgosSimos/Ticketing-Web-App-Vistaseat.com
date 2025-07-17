package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.EventCardDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.UserRole;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EventRepository eventRepository;

    @GetMapping("/home")
    public String home(Model model) {
        List<EventCardDto> cards = eventRepository.findAllWithAtLeastOneOccurrence()
                .stream().map(EventCardDto::from)
                .toList();
        model.addAttribute("cards", cards);
        return "index";
    }

    @GetMapping("/userSignUp")
    public String userSignup(){
        return "userSignUp";
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
