package com.unipi.gsimos.vistaseat.model;

import com.unipi.gsimos.vistaseat.customAnnotations.ValidEmail;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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

    // Multiple bookings can refer to a single event, event_id is a FK
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Multiple bookings can be made by a single user, user_id is a FK
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true) // Nullable for guests
    private User user;

    @Column(name = "first_name", nullable = false) // For registered users autocompleted with user.getFirstName()
    private String firstName;

    @Column(name = "last_name", nullable = false) //For registered users autocompleted with user.getLastName()
    private String lastName;

    @ValidEmail
    @Column(nullable = false) // For registered users autocompleted with user.getEmail()
    private String email;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Min(value = 0, message = "Booking price cannot be negative")
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    // A booking can have multiple tickets
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

}