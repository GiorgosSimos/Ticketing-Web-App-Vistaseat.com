package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.PaymentMethods;
import com.unipi.gsimos.vistaseat.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private Long id;
    private LocalDateTime paymentDateTime;
    private BigDecimal amount;
    private PaymentMethods paymentMethod;
    private PaymentStatus status;
    private Long bookingId;
}
