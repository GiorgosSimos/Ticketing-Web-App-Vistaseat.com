package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Payment;
import com.unipi.gsimos.vistaseat.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Payment findByBookingId(Long bookingId);

    Payment findByBookingIdAndStatus(Long bookingId, PaymentStatus status);
}
