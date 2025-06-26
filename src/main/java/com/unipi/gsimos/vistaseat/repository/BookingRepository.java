package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countBookingsByEventOccurrenceId(Long eventOccurrenceId);
}
