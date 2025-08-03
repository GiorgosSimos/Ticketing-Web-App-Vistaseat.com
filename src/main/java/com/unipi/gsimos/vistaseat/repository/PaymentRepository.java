package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.Payment;
import org.springframework.data.repository.CrudRepository;

public interface PaymentRepository extends CrudRepository<Payment, Long> {
}
