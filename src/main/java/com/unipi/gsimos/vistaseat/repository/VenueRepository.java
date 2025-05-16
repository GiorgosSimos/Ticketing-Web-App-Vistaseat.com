package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    Page<Venue> findVenueByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT COUNT(v) FROM Venue v WHERE v.managedBy.id = :userId")
    long countVenuesManagedByUser(@Param("userId") Long userId);

}
