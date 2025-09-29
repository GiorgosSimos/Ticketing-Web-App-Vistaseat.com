package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.TestimonialDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.mapper.UserMapper;
import com.unipi.gsimos.vistaseat.model.Testimonial;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.service.TestimonialService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TestimonialController {

    private final UserRepository userRepository;
    private final TestimonialService testimonialService;

    @GetMapping("/adminDashboard/manageTestimonials")
    public String manageTestimonials (@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String sort,
                                      @RequestParam(defaultValue = "desc") String sortDirection,
                                      Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TestimonialDto> testimonials = testimonialService.getAllTestimonials(pageable);

        List<TestimonialDto> testimonialsList = new ArrayList<>(testimonials.getContent());

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("testimonials", testimonialsList);

        // Sort in descending/ascending order by rating
        if ("rating".equals(sort)) {
            if ("asc".equalsIgnoreCase(sortDirection)) {
                testimonialsList.sort(Comparator.comparing(TestimonialDto::getRating));
            } else {
                testimonialsList.sort(Comparator.comparing(TestimonialDto::getRating).reversed());
            }
        }

        // Sort by number of rating
        model.addAttribute("sort", sort);
        model.addAttribute("sortDirection", sortDirection);

        // Paging controls
        model.addAttribute("currentPage", testimonials.getNumber() + 1);
        model.addAttribute("totalPages", testimonials.getTotalPages());

        return "manageTestimonials";

    }

    @PostMapping("/adminDashboard/manageTestimonials/toggleVisibility/{testimonialId}")
    public String toggleTestimonialVisibility (@PathVariable Long testimonialId,
                                               RedirectAttributes redirectAttributes) {

        try {
            testimonialService.toggleTestimonialVisibility(testimonialId);
            redirectAttributes.addFlashAttribute("message", "Visibility has changed successfully!");
            return "redirect:/adminDashboard/manageTestimonials?toggleVisibility=success";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Testimonial with id " + testimonialId + " not found!");
            return "redirect:/adminDashboard/manageTestimonials?toggleVisibility=failed";
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", "Access denied!");
            return "redirect:/adminDashboard/manageTestimonials?toggleVisibility=failed";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Oops, an unexpected error occurred: "+e.getMessage());
            return "redirect:/adminDashboard/manageTestimonials?toggleVisibility=failed";
        }

    }

    @PostMapping("/adminDashboard/manageTestimonials/delete/{testimonialId}")
    public String deleteTestimonial(@PathVariable Long testimonialId,
                                    RedirectAttributes redirectAttributes) {

        try {
            testimonialService.deleteTestimonial(testimonialId);
            redirectAttributes.addFlashAttribute("message", "Testimonial deleted successfully");
            return "redirect:/adminDashboard/manageTestimonials?success";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("Error", "Oops, something went wrong: " + ex.getMessage());
            return "redirect:/adminDashboard/manageTestimonials";
        }

    }

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
