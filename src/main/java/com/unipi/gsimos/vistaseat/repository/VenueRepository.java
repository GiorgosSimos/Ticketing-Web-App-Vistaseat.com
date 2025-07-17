package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    Page<Venue> findVenueByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Venue> findTop5ByNameContainingIgnoreCase(String venueName);

    /**
     * Counts the number of venues managed by a specific user (admin).
     * <p>
     * This method uses a custom JPQL (Java Persistence Query Language) query to retrieve
     * the count of {@code Venue} entities associated with a particular user ID.
     * JPQL is an object-oriented query language similar to SQL but operates on entity objects
     * rather than directly on database tables.
     *
     * @param userId the ID of the user (admin) managing the venues
     * @return the total number of venues managed by the specified user
     */
    @Query("SELECT COUNT(v) FROM Venue v WHERE v.managedBy.id = :userId")
    long countVenuesManagedByUser(@Param("userId") Long userId);

    boolean existsByName(String name);

}
