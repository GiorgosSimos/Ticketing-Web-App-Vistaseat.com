package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Payment;
import com.unipi.gsimos.vistaseat.model.PaymentStatus;
import org.springframework.data.repository.CrudRepository;

public interface PaymentRepository extends CrudRepository<Payment, Long> {

    Payment findByBookingIdAndStatus(Long bookingId, PaymentStatus status);
}
