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
    private BigDecimal rating;
    private String review;
    private LocalDateTime createdAt;
}
