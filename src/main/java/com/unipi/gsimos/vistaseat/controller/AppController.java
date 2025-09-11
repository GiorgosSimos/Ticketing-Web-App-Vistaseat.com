package com.unipi.gsimos.vistaseat.controller;


import com.unipi.gsimos.vistaseat.dto.ContactFormDto;
import com.unipi.gsimos.vistaseat.model.ContactCategory;
import com.unipi.gsimos.vistaseat.model.ContactMessage;
import com.unipi.gsimos.vistaseat.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class AppController {

    private final ContactMessageService contactMessageService;

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

        model.addAttribute("contactForm", new ContactFormDto());
        model.addAttribute("categories", ContactCategory.values());

        return "contact";

    }

    @PostMapping("/submitContactForm")
    public String submitContactForm(@ModelAttribute("contactForm") ContactFormDto contactForm,
                                    BindingResult bindingResult,
                                    Model model) {

        if (contactForm.getHp() != null && !contactForm.getHp().isBlank()) {
            bindingResult.reject("error", "Spam detected");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please correct the errors and try again");
        }

        ContactMessage contactMessage = new ContactMessage();

        contactMessage.setName(contactForm.getName());
        contactMessage.setEmail(contactForm.getEmail());
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
