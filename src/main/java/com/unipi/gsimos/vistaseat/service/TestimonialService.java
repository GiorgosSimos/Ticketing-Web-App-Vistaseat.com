package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.TestimonialDto;
import com.unipi.gsimos.vistaseat.model.Testimonial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TestimonialService {

    void createTestimonial(Testimonial  testimonial);

    void deleteTestimonial(Long  testimonialId);

    void toggleTestimonialVisibility(Long testimonialId);

    List<TestimonialDto> getTestimonialsByDisplayFlag (boolean displayFlag);

    Page<TestimonialDto> getAllTestimonials(Pageable pageable);
}
