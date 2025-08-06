package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {


    boolean existsByTicketNumber(String ticketNumber);

    List<Ticket> findAllByBookingId(Long bookingId);
}
