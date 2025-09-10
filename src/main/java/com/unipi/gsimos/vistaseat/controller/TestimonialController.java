package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.mapper.UserMapper;
import com.unipi.gsimos.vistaseat.model.Testimonial;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.service.TestimonialService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class TestimonialController {

    private final UserRepository userRepository;
    private final TestimonialService testimonialService;

    @PreAuthorize("hasRole('REGISTERED')")
    @GetMapping("/userAccount/testimonials")
    public String writeTestimonial(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email: " + email + " not found"));

        UserDto userDto = UserMapper.toUserDto(user);
        model.addAttribute("user", userDto);

        return "writeTestimonial";
    }

    @PreAuthorize("hasRole('REGISTERED')")
    @PostMapping("/userAccount/testimonials/submit")
    public String submitTestimonial(@RequestParam BigDecimal rating,
                                    @RequestParam String review,
                                    RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email: " + email + " not found"));

        Testimonial testimonial = new Testimonial();

        testimonial.setRating(rating);
        testimonial.setReview(review);
        testimonial.setUser(user);

        try {
            testimonialService.createTestimonial(testimonial);
            redirectAttributes.addFlashAttribute("message", "Testimonial submitted successfully!");
            return  "redirect:/userAccount/testimonials?success";
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred!");
            return "redirect:/userAccount/testimonials?error";
        }

    }

}
