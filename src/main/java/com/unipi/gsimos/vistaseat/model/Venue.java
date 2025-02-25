package com.unipi.gsimos.vistaseat.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @Column(name = "venue_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    private int zipcode;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "capacity", nullable = false)
    @Min(value = 0, message = "Capacity of a venue cannot be negative")
    private int capacity;

    // A venue hosts multiple events
    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Event> events = new ArrayList<>();
}
