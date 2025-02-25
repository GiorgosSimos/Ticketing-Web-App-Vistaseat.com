package com.unipi.gsimos.vistaseat.model;

import jakarta.validation.constraints.Min;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    @Min(value = 0, message = "Event price cannot be negative")
    private BigDecimal price;

    @Column(nullable = false)
    @Min(value = 0, message = "Duration of an event cannot be negative")
    private int duration;

    //WARNING: Initialization of available seats should match the capacity of the corresponding venue
    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Many events are hosted into one venue, venue_id is a FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    // Many events are managed by one user (admin), admin_id is a FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User managedBy;

    // An event can have multiple bookings
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Booking> bookings;
}

