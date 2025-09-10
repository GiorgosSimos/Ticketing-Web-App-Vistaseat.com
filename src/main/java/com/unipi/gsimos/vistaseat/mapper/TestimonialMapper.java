package com.unipi.gsimos.vistaseat.mapper;

import com.unipi.gsimos.vistaseat.dto.TestimonialDto;
import com.unipi.gsimos.vistaseat.model.Testimonial;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestimonialMapper {

    public Testimonial toEntity(TestimonialDto testimonialDto) {
        Testimonial testimonial = new Testimonial();

        testimonial.setRating(testimonialDto.getRating());
        testimonial.setReview(testimonialDto.getReview());
        testimonial.setCreatedAt(testimonialDto.getCreatedAt());

        return testimonial;
    }

    public TestimonialDto toDto(Testimonial testimonial) {
        TestimonialDto testimonialDto = new TestimonialDto();

        testimonialDto.setId(testimonial.getId());
        testimonialDto.setRating(testimonial.getRating());
        testimonialDto.setReview(testimonial.getReview());
        testimonialDto.setCreatedAt(testimonial.getCreatedAt());

        return testimonialDto;
    }
}
