package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.VenueDto;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.Venue;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.VenueService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//A Web controller (@Controller) works with HTML forms (using @ModelAttribute, returns view names for Thymeleaf).
@Controller
public class VenueController {

    private final VenueService venueService;
    private final VenueRepository venueRepository;


    public VenueController(VenueService venueService, VenueRepository venueRepository) {
        this.venueService = venueService;
        this.venueRepository = venueRepository;
    }

    @GetMapping("/adminDashboard/manageVenues")
    public String manageUsers(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "8") int size,
                              @RequestParam(required = false) String searchQuery,
                              Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Pageable pageable = PageRequest.of(page, size);
        Page<VenueDto> venuesPage;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            venuesPage = venueService.searchVenueByName(searchQuery.trim(), pageable);
        } else {
            venuesPage = venueService.getAllVenues(page, size);
        }

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        model.addAttribute("venues", venuesPage.getContent());

        // .getNumber() is zero-based, +1 is used for display purposes
        model.addAttribute("currentPage", venuesPage.getNumber() + 1);
        model.addAttribute("totalPages", venuesPage.getTotalPages());

        model.addAttribute("searchQuery", searchQuery);

        return "manageVenues";
    }

    @GetMapping("/adminDashboard/manageVenues/addVenue")
    public String addVenue(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());

        return "addVenue";
    }

    /**
     * Handles POST requests for creating a new venue.
     * <p>
     * Binds form data to a {@link VenueDto}, sets the current authenticated admin as the venue's admin,
     * and attempts to save the new venue using the service layer.
     * <ul>
     *   <li>If creation is successful, a success message is added to flash attributes and the user is redirected to the manage venues page.</li>
     *   <li>If an error occurs, an error message is added to flash attributes and the user is redirected back to the add venue form.</li>
     * </ul>
     * @param venueDto             The data transfer object containing venue details from the form.
     *                             The {@code @ModelAttribute} annotation
     *                             that binds submitted form fields to this object.
     * @param redirectAttributes   Attributes for passing flash messages on redirect.
     * @return Redirect to the appropriate page depending on the operation result.
     */
    @PostMapping("/adminDashboard/manageVenues/addVenue/create")
    public String createVenue(@ModelAttribute VenueDto venueDto,
                              RedirectAttributes redirectAttributes) {

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

    @GetMapping("/adminDashboard/manageVenues/editVenue/{venueId}")
    public String showEditVenueForm(@PathVariable Long venueId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("venue", venue);

        return "editVenue";
    }

    // Handle form submission (POST)
    @PostMapping("/adminDashboard/manageVenues/editVenue/{venueId}")
    public String editVenue(@PathVariable Long venueId,
                            @ModelAttribute VenueDto venueDto,
                            RedirectAttributes redirectAttributes) {

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(()-> new RuntimeException("Venue not found"));

        venue.setName(venueDto.getName());
        venue.setStreet(venueDto.getStreet());
        venue.setNumber(venueDto.getNumber());
        venue.setZipcode(venueDto.getZipcode());
        venue.setCity(venueDto.getCity());
        venue.setCapacity(venueDto.getCapacity());

        try {
            venueRepository.save(venue);
            redirectAttributes.addFlashAttribute("message", "Venue updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Venue could not be updated because of an unexpected error: " + e.getMessage());
            return "redirect:/adminDashboard/manageVenues/editVenue/" + venueId;
        }

        return "redirect:/adminDashboard/manageVenues";
    }

    @PostMapping("adminDashboard/manageVenues/delete/{id}")
    public String deleteVenue(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            venueService.deleteVenue(id);
            redirectAttributes.addFlashAttribute("message", "Venue was deleted successfully!");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "Venue not found!");
        }

        return "redirect:/adminDashboard/manageVenues";
    }
}
