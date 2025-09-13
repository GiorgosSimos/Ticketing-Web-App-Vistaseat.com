package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.TestimonialDto;
import com.unipi.gsimos.vistaseat.mapper.TestimonialMapper;
import com.unipi.gsimos.vistaseat.model.Testimonial;
import com.unipi.gsimos.vistaseat.repository.TestimonialRepository;
import com.unipi.gsimos.vistaseat.service.TestimonialService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestimonialServiceImpl implements TestimonialService {

    private final TestimonialRepository testimonialRepository;
    private final TestimonialMapper testimonialMapper;

    @Transactional
    @Override
    public void createTestimonial(Testimonial testimonial) {
        // Execute additional validation checks

        testimonialRepository.save(testimonial);
    }

    @Override
    public List<TestimonialDto> getTestimonialsByDisplayFlag(boolean displayFlag) {

        List<Testimonial> testimonials = testimonialRepository.findAllByDisplayed(true);

        return testimonials.stream().map(testimonialMapper::toDto).toList();
    }
}
