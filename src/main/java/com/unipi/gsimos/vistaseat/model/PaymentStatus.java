package com.unipi.gsimos.vistaseat.model;

public enum PaymentStatus {
    COMPLETED, // Payment was successful
    FAILED, // Payment attempt failed (insufficient funds, network error, etc.)
    REFUNDED, // Amount was refunded to the customer
}
