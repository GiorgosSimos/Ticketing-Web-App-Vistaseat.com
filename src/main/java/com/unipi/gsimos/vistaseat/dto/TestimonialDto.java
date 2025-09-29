package com.unipi.gsimos.vistaseat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialDto {

    private Long id;
    private Long userId;
    private String author;
    private BigDecimal rating;
    private String review;
    private boolean visible;
    private LocalDateTime createdAt;

    // Calculation for star depiction in testimonials section (home page)
    private int fullStars;
    private boolean halfStars;
    private int emptyStars;

}
