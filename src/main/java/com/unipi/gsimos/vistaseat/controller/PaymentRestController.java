package com.unipi.gsimos.vistaseat.controller;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import com.unipi.gsimos.vistaseat.model.Booking;
import com.unipi.gsimos.vistaseat.model.PaymentMethods;
import com.unipi.gsimos.vistaseat.repository.BookingRepository;
import com.unipi.gsimos.vistaseat.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentRestController {

    private final PayPalHttpClient client;
    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;


    private final ConcurrentMap<String, Long> orderToBooking = new ConcurrentHashMap<>();

    /**
     * Creates a PayPal (Sandbox) order for the given booking and returns its ID.
     * The amount is re-derived server-side from the booking to prevent tampering.
     *
     * @param bookingId the booking to be paid
     * @return a map containing the PayPal order ID under key "id"
     * @throws Exception if the PayPal Orders create call fails
     */
    @PostMapping("/{bookingId}/paypal/order")
    public ResponseEntity<?> createOrder(@PathVariable Long bookingId) throws Exception{

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new EntityNotFoundException("Booking not found"));

        var now = LocalDateTime.now();
        if (booking.getExpiresAt() != null && !booking.getExpiresAt().isAfter(now)) {
            return ResponseEntity.status(HttpStatus.GONE)  // 410 Gone
                    .body(Map.of("error", "This booking has expired and can no longer be paid."));
        }

        String amount = booking.getTotalAmount().setScale(2, RoundingMode.HALF_UP).toString();

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");
        orderRequest.applicationContext(new ApplicationContext()
                .brandName("Vistaseat")
                .landingPage("LOGIN")
                .userAction("PAY_NOW"))
            .purchaseUnits(List.of(new PurchaseUnitRequest()
                    .referenceId("booking-" + bookingId) // extra breadcrumbs
                    .customId(bookingId.toString())
                    .amountWithBreakdown(new AmountWithBreakdown()
                            .currencyCode("EUR")
                            .value(amount)
                    )));

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("PayPal-Request-Id", UUID.randomUUID().toString());
        request.prefer("return=representation");
        request.requestBody(orderRequest);

        HttpResponse<Order> response = client.execute(request);
        String orderId = response.result().id();

        // correlate for capture fallback
        orderToBooking.put(orderId, bookingId);

        return ResponseEntity.ok(Map.of("id", orderId));
    }

    /**
     * Captures an approved PayPal order and finalizes the booking payment.
     * Expects {"orderId": "..."}; uses purchase unit custom_id to resolve the booking.
     *
     * @param body request JSON containing the PayPal orderId
     * @return 200 OK on successful capture; otherwise an error response
     * @throws Exception if the PayPal Orders capture call fails
     */
    @PostMapping("/paypal/capture")
    public ResponseEntity<?> capture(@RequestBody Map<String, String> body) throws Exception{

        String orderId = body.get("orderId");

        if (orderId == null || orderId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing orderId");
        }

        // prefer bookingId from the client
        Long bookingId = null;
        String bookingIdStr = body.get("bookingId");
        if (bookingIdStr != null && !bookingIdStr.isBlank()) {
            bookingId = Long.parseLong(bookingIdStr);
        } else {
            // fallback to server-side correlation (from createOrder)
            bookingId = orderToBooking.get(orderId);
        }
        if (bookingId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Could not resolve bookingId for order " + orderId);
        }

        // Re-check expiry just before capturing
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        var now = LocalDateTime.now();
        if (booking.getExpiresAt() != null && !booking.getExpiresAt().isAfter(now)) {
            // Do NOT capture. Let the PayPal order lapse; or optionally void if you implement that.
            return ResponseEntity.status(HttpStatus.GONE)
                    .body("This booking has expired and cannot be paid.");
        }

        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        request.header("PayPal-Request-Id", orderId); // idempotent capture
        request.requestBody(new OrderRequest());    // empty body

        HttpResponse<Order> response = client.execute(request);
        var order = response.result();

        if (!"COMPLETED".equals(order.status())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(request);
        }

        // clean correlation to avoid leaks
        orderToBooking.remove(orderId);

        paymentService.paymentCompleted(bookingId, PaymentMethods.PAYPAL);
        return ResponseEntity.ok().build();
    }
}
