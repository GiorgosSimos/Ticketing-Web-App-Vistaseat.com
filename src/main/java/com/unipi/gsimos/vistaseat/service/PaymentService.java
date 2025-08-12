package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.model.PaymentMethods;

public interface PaymentService {

    void paymentCompleted(Long bookingId, PaymentMethods paymentMethod);
}
