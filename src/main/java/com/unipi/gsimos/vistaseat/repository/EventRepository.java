package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findEventByNameContainingIgnoreCase(String name, Pageable pageable);


}
