package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.TestimonialDto;
import com.unipi.gsimos.vistaseat.model.Testimonial;

import java.util.List;

public interface TestimonialService {

    void createTestimonial(Testimonial  testimonial);

    List<TestimonialDto> getTestimonialsByDisplayFlag (boolean displayFlag);
}
