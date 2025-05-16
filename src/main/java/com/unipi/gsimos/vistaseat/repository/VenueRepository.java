package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    Page<Venue> findVenueByNameContainingIgnoreCase(String name, Pageable pageable);
}
