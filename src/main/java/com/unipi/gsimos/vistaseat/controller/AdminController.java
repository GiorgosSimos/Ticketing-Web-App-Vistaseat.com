package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.model.UserRole;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.ui.Model;
import com.unipi.gsimos.vistaseat.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.security.Principal;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;

    public AdminController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "index"; // This should correspond to index.html in /templates/
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

    @GetMapping("/userSignUp")
    public String userSignup(){
        return "userSignUp";
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

        // Total counts
        model.addAttribute("totalUsers", userService.countAllUsers());

        // Recent Users
        model.addAttribute("recentUsers", userService.getLast10Users());

        return "adminDashboard";
    }

    @GetMapping("adminDashboard/account")
    public String adminAccount(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("phone", user.getPhone());

        return "adminDashboardAccount";
    }

    @PostMapping("/adminDashboard/account/update")
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

        userRepository.save(user);

        // Refresh the user in SecurityContextHolder after update
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,                      // the updated User object
                null,                     // credentials -> not providing a password, the user is already authenticated
                user.getAuthorities());  // the roles/authorities of the user
        SecurityContextHolder.getContext().setAuthentication(authToken);

        redirectAttributes.addFlashAttribute("successMessage",
                "Your account details ware updated successfully!");

        return "redirect:/adminDashboard/account";
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
                              @RequestParam(defaultValue = "8") int size,
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
