package com.unipi.gsimos.vistaseat.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true) // Nullable for guests
    private User user;

    @Column(nullable = false) // For registered users autocompleted with user.getEmail()
    private String email;

    @Column(name = "first_name", nullable = false) // For registered users autocompleted with user.getFirstName()
    private String firstName;

    @Column(name = "last_name", nullable = false) //For registered users autocompleted with user.getLastName()
    private String lastName;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @Column(nullable = false)
    private double totalPrice;
}