package com.unipi.gsimos.vistaseat.model;

import jakarta.validation.constraints.Min;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Min(value = 0, message = "Event price cannot be negative")
    private double price;

    @Min(value = 0, message = "Duration of an event cannot be negative")
    private int duration;

    //TODO Initialization of available seats should match the capacity of the corresponding venue
    private int availableSeats;

    private LocalDateTime eventDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

}
