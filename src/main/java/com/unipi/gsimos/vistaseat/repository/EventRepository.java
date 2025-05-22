package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {


}
