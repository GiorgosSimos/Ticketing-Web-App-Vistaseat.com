package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.*;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.TestimonialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AppController {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
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

    @GetMapping("/faq")
    public String getFAQ(Model model) {

        model.addAttribute("appName", "Vistaseat.com");
        model.addAttribute("supportEmail", "support@vistaseat.com");

        return "faq";

    }

}
