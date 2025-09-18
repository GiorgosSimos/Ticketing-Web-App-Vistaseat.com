package com.unipi.gsimos.vistaseat.model;

public enum BookingStatus {
    PENDING, // Booking awaits the completion of a payment transaction.
    CONFIRMED, // Payment Transaction is complete. Booking is active.
    CANCELLED, // Reservation expired after 15-minute window, payment failed.
    REFUNDED // Event was postponed or cancelled, user chose to cancel
}
