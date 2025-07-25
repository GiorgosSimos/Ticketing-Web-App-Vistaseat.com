package com.unipi.gsimos.vistaseat.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event_occurrences")
public class EventOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_occurrence_id")
    private Long id;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    @Min(value = 0, message = "Event price cannot be negative")
    private BigDecimal price;

    @Column(nullable = false)
    @Min(value = 0, message = "Duration of an event cannot be negative")
    private int duration;

    /**
     * Total seat capacity for this event occurrence.
     * Set once on creation — must not exceed the hosting venue’s capacity and is never updated thereafter.
     */
    @Column(name = "available_seats", nullable = false, updatable = false)
    private int availableSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @OneToMany(mappedBy = "eventOccurrence", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Booking> bookings;

    /**
     * Tickets already sold for this occurrence.
     * <p>
     * Computed by the database via {@code @Formula}; never persisted here.
     */
    @Formula("""
   (SELECT COALESCE(SUM(b.number_of_tickets), 0)
      FROM bookings b
     WHERE b.event_occurrence_id = event_occurrence_id)
""")
    private int seatsSold;

    /**
     * Remaining seats (availableSeats − seatsSold). Not stored in the DB.
     */
    @Transient
    public int getRemainingSeats() {
        return availableSeats - seatsSold;
    }

    /**
     * @return {@code true} when no seats remain.
     */
    @Transient
    public boolean isSoldOut() {
        return seatsSold >= availableSeats;
    }


}
