package com.unipi.gsimos.vistaseat.model;

public enum BookingStatus {
    PENDING, // Booking awaits the completion of a payment transaction.
    ACTIVE, // Payment Transaction is complete. Booking is active until the date of the occurrence
    COMPLETED, // Event occurrence date has passed
    CANCELLED // Booking is canceled
}
