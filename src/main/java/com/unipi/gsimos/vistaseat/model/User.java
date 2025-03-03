package com.unipi.gsimos.vistaseat.model;

import com.unipi.gsimos.vistaseat.customAnnotations.ValidEmail;
import jakarta.persistence.*;
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
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(nullable = false)
    private String phone;

    @ValidEmail
    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // An admin manages multiple events
    @OneToMany(mappedBy = "managedBy", fetch = FetchType.LAZY)
    private List<Event> managedEvents = new ArrayList<>();

    // An admin manages multiple venues
    @OneToMany(mappedBy = "managedBy", fetch = FetchType.LAZY)
    private List<Venue> managedVenues = new ArrayList<>();
}
