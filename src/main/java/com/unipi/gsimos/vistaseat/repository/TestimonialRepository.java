package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    List<Testimonial> findAllByDisplayed(boolean displayed);
}
