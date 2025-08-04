package com.unipi.gsimos.vistaseat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ]{2,30}(?:[ '--][A-Za-zÀ-ÖØ-öø-ÿ]{2,30})?$",
            message = "Invalid first name")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ]{2,40}(?:[ '--][A-Za-zÀ-ÖØ-öø-ÿ]{2,40})*$",
            message = "Invalid last name")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "password", nullable = false)
    private String password;

    // Matches a phone number that may optionally start with a plus sign and contain
    // between 7 and 20 characters made up of digits, dots, spaces, parentheses, or dashes
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$", message = "Invalid phone number format")
    @Column(nullable = false)
    private String phone;

    @Email(message = "Invalid e-mail address")
    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.REGISTERED; // Default role is registered User

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true; // All users are active when created

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime registrationDate;

    // An admin manages multiple events
    @JsonIgnore
    @OneToMany(mappedBy = "managedBy", fetch = FetchType.LAZY)
    private List<Event> managedEvents = new ArrayList<>();

    // A user (admin) can manage many venues, but each venue is managed by one user.
    @JsonIgnore
    @OneToMany(mappedBy = "managedBy", fetch = FetchType.LAZY)
    private List<Venue> managedVenues = new ArrayList<>();

    // Spring Security Methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }

    @Override
    public String getUsername() {
        return email; // Users are authenticated with their email
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
