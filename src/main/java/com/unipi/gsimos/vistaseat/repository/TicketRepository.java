package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {


    boolean existsByTicketNumber(String candidate);
}
