package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.TestimonialDto;
import com.unipi.gsimos.vistaseat.mapper.TestimonialMapper;
import com.unipi.gsimos.vistaseat.model.Testimonial;
import com.unipi.gsimos.vistaseat.repository.TestimonialRepository;
import com.unipi.gsimos.vistaseat.service.TestimonialService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Transactional
    @Override
    public void toggleTestimonialVisibility(Long testimonialId) {

        Testimonial testimonial = testimonialRepository
                .findById(testimonialId).orElseThrow(()-> new EntityNotFoundException("Testimonial not found with id: " + testimonialId));

        testimonial.setDisplayed(!testimonial.isDisplayed());
        testimonialRepository.save(testimonial);
    }

    @Override
    public void deleteTestimonial(Long testimonialId) {

        Testimonial testimonial = testimonialRepository.findById(testimonialId)
                .orElseThrow(()-> new EntityNotFoundException("Testimonial not found with id: " + testimonialId));

        //Perform additional checks if necessary
        testimonialRepository.delete(testimonial);

    }

    @Override
    public List<TestimonialDto> getTestimonialsByDisplayFlag(boolean displayFlag) {

        List<Testimonial> testimonials = testimonialRepository.findAllByDisplayed(true);

        return testimonials.stream().map(testimonialMapper::toDto).toList();
    }

    @Override
    public Page<TestimonialDto> getAllTestimonials(Pageable pageable) {

        Page<Testimonial> testimonials = testimonialRepository.findAll(pageable);
        List<TestimonialDto> testimonialDtos = testimonials.stream().map(testimonialMapper::toDto).toList();
        return new PageImpl<>(testimonialDtos, pageable, testimonials.getTotalElements());
    }
}
