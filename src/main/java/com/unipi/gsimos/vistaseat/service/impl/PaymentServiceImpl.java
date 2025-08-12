package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.model.PaymentMethods;
import com.unipi.gsimos.vistaseat.service.BookingService;
import com.unipi.gsimos.vistaseat.service.PaymentService;
import com.unipi.gsimos.vistaseat.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingService bookingService;
    private final TicketService ticketService;

    @Override
    public void paymentCompleted(Long bookingId, PaymentMethods paymentMethod) {

        bookingService.confirmBooking(bookingId,  paymentMethod);
        ticketService.createTickets(bookingId);
    }
}
