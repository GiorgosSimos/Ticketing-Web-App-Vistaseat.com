package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.*;
import com.unipi.gsimos.vistaseat.mapper.UserMapper;
import com.unipi.gsimos.vistaseat.model.ContactCategory;
import com.unipi.gsimos.vistaseat.model.ContactMessage;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.ContactMessageService;
import com.unipi.gsimos.vistaseat.service.TestimonialService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AppController {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final ContactMessageService contactMessageService;
    private final UserRepository userRepository;
    private final TestimonialService testimonialService;

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

        List<TestimonialDto> testimonialDtos = testimonialService.getTestimonialsByDisplayFlag(true);

        model.addAttribute("testimonials", testimonialDtos);
        
        return "index";
    }


    @GetMapping("/termsOfUse")
    public String getTermsOfUse(Model model) {

        model.addAttribute("appName", "Vistaseat.com");
        model.addAttribute("companyName", "Vistaseat S.A.");
        model.addAttribute("companyAddress", "Dream Street 34, Athens, Greece");
        model.addAttribute("vatId", "EL0123456789");
        model.addAttribute("companyRegNo", "GEMI 0123456789");
        model.addAttribute("supportEmail", "support@vistaseat.com");
        model.addAttribute("jurisdiction", "Hellenic Republic");
        model.addAttribute("forum", "Athens, Greece");
        model.addAttribute("lastUpdated", LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")));

        return "termsOfUse";

    }

    @GetMapping("/aboutUs")
    public String getAboutUs(Model model) {

        model.addAttribute("appName", "Vistaseat.com");
        model.addAttribute("companyName", "Vistaseat S.A.");
        model.addAttribute("companyAddress", "Dream Street 34, Athens, Greece");
        model.addAttribute("region", "Greece and Europe");
        model.addAttribute("supportEmail", "support@vistaseat.com");

        return "aboutUs";

    }

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

    @GetMapping("/faq")
    public String getFAQ(Model model) {

        model.addAttribute("appName", "Vistaseat.com");
        model.addAttribute("supportEmail", "support@vistaseat.com");

        return "faq";

    }

}
