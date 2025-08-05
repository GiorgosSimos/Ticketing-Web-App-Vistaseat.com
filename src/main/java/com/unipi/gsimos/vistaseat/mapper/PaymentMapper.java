package com.unipi.gsimos.vistaseat.mapper;

import com.unipi.gsimos.vistaseat.dto.PaymentDto;
import com.unipi.gsimos.vistaseat.model.Booking;
import com.unipi.gsimos.vistaseat.model.Payment;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentMapper {

    private final BookingRepository bookingRepository;

    public Payment toEntity(PaymentDto paymentDto) {
        Payment payment = new Payment();

        payment.setPaymentDate(paymentDto.getPaymentDateTime());
        payment.setAmount(paymentDto.getAmount());
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setStatus(paymentDto.getStatus());

        Booking booking = bookingRepository.findById(paymentDto.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        payment.setBooking(booking);

        return payment;
    }

    public PaymentDto toDto(Payment payment) {

        PaymentDto paymentDto = new PaymentDto();

        paymentDto.setId(payment.getId());
        paymentDto.setPaymentDateTime(payment.getPaymentDate());
        paymentDto.setAmount(payment.getAmount());
        paymentDto.setPaymentMethod(payment.getPaymentMethod());
        paymentDto.setStatus(payment.getStatus());
        paymentDto.setBookingId(payment.getBooking().getId());

        return paymentDto;
    }
}
