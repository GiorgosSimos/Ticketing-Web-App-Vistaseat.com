package com.unipi.gsimos.vistaseat.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventOccurrenceDto {

    private Long id;

    private LocalDateTime eventDateTime;

    @Min(value = 0, message = "Event price cannot be negative")
    private BigDecimal price;

    @Min(value = 0, message = "Duration of an event cannot be negative")
    private int duration;

    /**
     * Total seat capacity for this event occurrence.
     * Set once on creation — must not exceed the hosting venue’s capacity and is never updated thereafter.
     */
    private int availableSeats;

    private Long eventId;

    private String eventName;

    private Long bookingCount;

    private int seatsSold;

    private BigDecimal occurrenceTotalRevenue;

    /**
     * Availability expressed as a decimal in the range {@code 0.0 – 1.0}
     * (returns 0 when remaining seats is 0).
     */
    private float getAvailability() {
        if  (seatsSold >= availableSeats) {
            return 0;
        }

        return (availableSeats - seatsSold) / (float)availableSeats;
    }

    /**
     * Returns the semantic availability category for this event occurrence,
     * based on the percentage of remaining seats using the {@link #getAvailability()} method.
     * <p>
     * The thresholds are:
     * <ul>
     *   <li><b>HIGH:</b> 50% - 100%</li>
     *   <li><b>MEDIUM:</b> 25% - 49%</li>
     *   <li><b>LOW:</b> 0.1% - 24%</li>
     *   <li><b>SOLD_OUT:</b> 0%</li>
     * </ul>
     *
     * @return the matching {@link AvailabilityLevel} constant
     */
    public AvailabilityLevel getAvailabilityLevel() {
        float pct = getAvailability() * 100;

        if (pct == 0)               return AvailabilityLevel.SOLD_OUT;
        if (pct <= 24)              return AvailabilityLevel.LOW;
        if (pct <= 49)              return AvailabilityLevel.MEDIUM;
        return AvailabilityLevel.HIGH;
    }

    /**
     * Enumeration of seat-availability categories, each representing
     * a range of remaining seat percentages.
     * Each constant includes a human-readable label and a corresponding Bootstrap CSS class
     * used for styling badges in the UI.
     */
    public enum AvailabilityLevel {

        HIGH   ("High",     "bg-success"),
        MEDIUM ("Medium",   "bg-secondary text-light"),
        LOW    ("Low",      "bg-warning text-dark"),
        SOLD_OUT("Sold-out","bg-danger");

        public final String label;
        public final String css;

        AvailabilityLevel(String label, String css) {
            this.label = label;
            this.css   = css;
        }
    }
}