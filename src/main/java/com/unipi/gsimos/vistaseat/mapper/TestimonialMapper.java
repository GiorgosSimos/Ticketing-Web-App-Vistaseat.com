package com.unipi.gsimos.vistaseat.mapper;

import com.unipi.gsimos.vistaseat.dto.TestimonialDto;
import com.unipi.gsimos.vistaseat.model.Testimonial;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TestimonialMapper {

    private final UserRepository userRepository;

    public Testimonial toEntity(TestimonialDto testimonialDto) {
        Testimonial testimonial = new Testimonial();

        User user = userRepository.findById(testimonialDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user != null) {
            testimonial.setUser(user);
        }
        testimonial.setRating(testimonialDto.getRating());
        testimonial.setReview(testimonialDto.getReview());
        testimonial.setCreatedAt(testimonialDto.getCreatedAt());

        return testimonial;
    }

    public TestimonialDto toDto(Testimonial testimonial) {
        TestimonialDto testimonialDto = new TestimonialDto();

        // Calculate stars for visual depiction
        BigDecimal rating = testimonial.getRating();

        int fullStarsCount = rating.intValue(); // truncate any decimal part e.g. 3.5 -> 3
        boolean hasHalfStar = rating.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0; // rating mod 1 !=0
        int emptyStars = 5 - fullStarsCount - (hasHalfStar ? 1 : 0);

        testimonialDto.setId(testimonial.getId());
        testimonialDto.setUserId(testimonial.getUser().getId());
        testimonialDto.setAuthor(testimonial.getUser().getFirstName() + " " + testimonial.getUser().getLastName());
        testimonialDto.setRating(testimonial.getRating());
        testimonialDto.setReview(testimonial.getReview());
        testimonialDto.setVisible(testimonial.isDisplayed());
        testimonialDto.setCreatedAt(testimonial.getCreatedAt());
        testimonialDto.setFullStars(fullStarsCount);
        testimonialDto.setHalfStars(hasHalfStar);
        testimonialDto.setEmptyStars(emptyStars);

        return testimonialDto;
    }
}
