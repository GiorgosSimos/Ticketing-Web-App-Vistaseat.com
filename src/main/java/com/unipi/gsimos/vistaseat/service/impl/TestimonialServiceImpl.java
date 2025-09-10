package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.model.Testimonial;
import com.unipi.gsimos.vistaseat.repository.TestimonialRepository;
import com.unipi.gsimos.vistaseat.service.TestimonialService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestimonialServiceImpl implements TestimonialService {

    private final TestimonialRepository testimonialRepository;

    @Transactional
    @Override
    public void createTestimonial(Testimonial testimonial) {
        // Execute additional validation checks

        testimonialRepository.save(testimonial);
    }
}
