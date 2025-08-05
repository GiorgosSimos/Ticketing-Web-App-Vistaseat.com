package com.unipi.gsimos.vistaseat.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @Column(name = "booking_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Multiple bookings can refer to a single event occurrence
    @ManyToOne
    @JoinColumn(name = "event_occurrence_id", nullable = false)
    private EventOccurrence eventOccurrence;

    // Multiple bookings can be made by a single user, user_id is a FK
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true) // Nullable for guests
    private User user;

    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ]{2,30}(?:[ '--][A-Za-zÀ-ÖØ-öø-ÿ]{2,30})?$", message = "Invalid first name")
    @Column(name = "first_name", nullable = false) // For registered users autocompleted with user.getFirstName()
    private String firstName;

    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ]{2,40}(?:[ '--][A-Za-zÀ-ÖØ-öø-ÿ]{2,40})*$",
            message = "Invalid last name")
    @Column(name = "last_name", nullable = false) //For registered users autocompleted with user.getLastName()
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$", message = "Invalid phone number format")
    @Column(name="phone_number", nullable = false)
    private String phoneNumber;

    @Email(message = "Invalid e-mail address")
    @Column(nullable = false) // For registered users autocompleted with user.getEmail()
    private String email;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate = LocalDateTime.now();

    // One seat per ticket
    @Min(1)
    @Column(name = "number_of_tickets", nullable = false)
    private int numberOfTickets;

    @Min(value = 0, message = "Booking price cannot be negative")
    @Column(name = "tickets_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketsPrice; // tickets cost, before fees

    @Min(value = 0, message = "Service fee price cannot be negative")
    @Column(name = "service_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal serviceFee; // total fee for this booking

    @Min(value = 0, message = "Total amount cannot be negative")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // tickets cost + service fee, charged in the user

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING; // All booking status are PENDING until payment is complete

    @Column(name = "expires_at", nullable = true)
    private LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15); // set to now + 15 min on creation, if it is set to null -> status = COMPLETED

    // A booking can have multiple tickets
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

}