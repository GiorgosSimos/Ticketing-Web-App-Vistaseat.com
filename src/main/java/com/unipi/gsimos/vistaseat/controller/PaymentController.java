package com.unipi.gsimos.vistaseat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    @GetMapping("/api/payments/{bookingId}")
    public String makePayment(@PathVariable Long bookingId, Model model){
        return "makePayment";
    }
}
