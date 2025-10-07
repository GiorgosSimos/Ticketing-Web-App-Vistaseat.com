package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.ContactFormDto;
import com.unipi.gsimos.vistaseat.dto.ContactMessageDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.mapper.UserMapper;
import com.unipi.gsimos.vistaseat.model.ContactCategory;
import com.unipi.gsimos.vistaseat.model.ContactMessage;
import com.unipi.gsimos.vistaseat.model.ContactStatus;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.service.ContactMessageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService contactMessageService;
    private final UserRepository userRepository;


    @GetMapping("/contact")
    public String getContactForm(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // A user is logged-in if we have an auth object, it’s marked authenticated,
        // and it’s NOT the anonymous token/principal.
        boolean isLoggedIn =
                auth != null
                        && auth.isAuthenticated()
                        && !(auth instanceof AnonymousAuthenticationToken)
                        && (auth.getPrincipal() instanceof UserDetails);

        UserDto userDto = null;
        String fullName = null;

        if (isLoggedIn) {
            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElseThrow(() ->
                    new EntityNotFoundException("User with " + email + " not found"));
            userDto = UserMapper.toUserDto(user);
            fullName = user.getFirstName() + " " + user.getLastName();
        }

        model.addAttribute("contactForm", new ContactFormDto());
        model.addAttribute("categories", ContactCategory.values());
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("fullName", fullName);
        model.addAttribute("user", userDto);

        return "contact";

    }

    @PostMapping("/submitContactForm")
    public String submitContactForm(@ModelAttribute("contactForm") ContactFormDto contactForm,
                                    BindingResult bindingResult,
                                    Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (contactForm.getHp() != null && !contactForm.getHp().isBlank()) {
            bindingResult.reject("error", "Spam detected");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please correct the errors and try again");
        }

        ContactMessage contactMessage = new ContactMessage();

        contactMessage.setName(contactForm.getName());
        contactMessage.setEmail(contactForm.getEmail());
        if (       auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getPrincipal() instanceof UserDetails) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("User with email:" + email + " not found"));
            contactMessage.setUser(user);
        }
        contactMessage.setSubject(contactForm.getSubject());
        contactMessage.setCategory(contactForm.getCategory());
        contactMessage.setMessage(contactForm.getMessage());

        try {
            contactMessageService.createContactMessage(contactMessage);
            model.addAttribute("success", true);
            model.addAttribute("contactForm", new ContactFormDto());
            return "/contact";
        }
        catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred");
            return "/contact";
        }

    }

    @GetMapping("/adminDashboard/manageContactMessages")
    public String manageContactMessages (@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String searchQuery,
                                         Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by( "createdAt").descending());
        Page<ContactMessageDto> messages;

        if(searchQuery != null) {
            messages = contactMessageService.getContactMessagesByAuthor(searchQuery, pageable);
        } else {
            messages = contactMessageService.getAllContactMessages(pageable);
        }

        List<ContactMessageDto> contactMessages = new ArrayList<>(messages.getContent());

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("messages", contactMessages);
        model.addAttribute("RESOLVED", ContactStatus.RESOLVED);

        // Paging controls
        model.addAttribute("currentPage", messages.getNumber() + 1);
        model.addAttribute("totalPages", messages.getTotalPages());

        return "manageContactMessages";

    }

    @PostMapping("/adminDashboard/manageContactMessages/resolve/{contactMessageId}")
    public String resolveIssue(@PathVariable Long contactMessageId,
                               @RequestParam String adminNotes,
                               RedirectAttributes redirectAttributes) {

        try {
            contactMessageService.resolveContactMessage(contactMessageId, adminNotes);
            redirectAttributes.addFlashAttribute("message", "Issue of this contact message is resolved!");
            return "redirect:/adminDashboard/manageContactMessages?resolved=true";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Error:" + e.getMessage());
            return "redirect:/adminDashboard/manageContactMessages";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Oops, an unexpected error occurred");
            return "redirect:/adminDashboard/manageContactMessages";
        }
    }

    @PostMapping("/adminDashboard/manageContactMessages/delete/{contactMessageId}")
    public String deleteContactMessage(@PathVariable Long contactMessageId,
                                       RedirectAttributes redirectAttributes) {

        try {
            contactMessageService.deleteContactMessage(contactMessageId);
            redirectAttributes.addFlashAttribute("message", "Contact message deleted successfully");
            return "redirect:/adminDashboard/manageContactMessages?deleted=true";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/adminDashboard/manageContactMessages";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred");
            return "redirect:/adminDashboard/manageContactMessages";
        }
    }
}
