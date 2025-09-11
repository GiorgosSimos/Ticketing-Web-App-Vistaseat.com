package com.unipi.gsimos.vistaseat.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
@Entity
@Table(name = "testimonials")
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Rating between 0 and 5 starts, incrementing by 0.5
    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Digits(integer = 1, fraction = 1)           // matches DECIMAL(2,1)
    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    // The written review text
    @NotBlank
    @Size(min = 5, max = 500)
    @Column(nullable = false, length = 500)
    private String review;

    // Testimonials are hidden by default
    @Column(nullable = false)
    private boolean displayed = false;

    // Creation timestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationship: Many testimonials -> One user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
