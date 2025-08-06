package com.unipi.gsimos.vistaseat.utilities;

import com.unipi.gsimos.vistaseat.model.EventType;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;

/**
        * Utility for minting unique, human-readable ticket numbers.
 *
         * <p>A generated code has the shape
 * <pre>{@code <PREFIX><BODY><CHECK>}</pre>
        * where
 * <ul>
 *   <li>{@code <PREFIX>} – 3-letter event-type mnemonic
 *        (<i>THT</i>, <i>CIN</i>, <i>MCN</i>, <i>SPT</i>, <i>MSM</i>, <i>ARC</i>)</li>
        *   <li>{@code <BODY>}   – 9-digit random decimal, zero-padded</li>
        *   <li>{@code <CHECK>}  – 1-digit Luhn (mod-10) checksum for the body</li>
        * </ul>
        *
        * <p>Example: {@code THT1234567897}
        *
        * <h2>Thread safety</h2>
        * The class is stateless and uses a single {@link java.security.SecureRandom}
 * instance, which is thread-safe; therefore {@link #generate(EventType)} may
 * be called concurrently without additional synchronisation.
        *
        * <h2>Collisions</h2>
        * Each prefix has 1 billion possible bodies; the birthday-paradox collision
 * probability is < 10<sup>-9</sup> per insert at typical ticketing volumes.
        * Follows a UNIQUE constraint on the persisted column.
 */
@Component
public class TicketNumberGenerator {

    private static final Map<EventType, String> PREFIXES = Map.of(
            EventType.THEATER, "THT",
            EventType.CINEMA, "CIN",
            EventType.CONCERT, "MCN",
            EventType.SPORTS, "SPT",
            EventType.MUSEUM, "MSM",
            EventType.ARCHAEOLOGICAL, "ARC"
    );

    // SecureRandom is a class that provides a cryptographically strong random number generator.
    private static final SecureRandom RAND =  new SecureRandom();

    /**
     * Returns something like "THT1234567897"
     * └── "THT"                      prefix (3)
     *     "123456789"                body  (9)
     *               "7"              Luhn check digit (1)
     */
    public String generate(EventType eventType) {

        String prefix = Optional.ofNullable(PREFIXES.get(eventType)).
                orElseThrow(() -> new IllegalArgumentException("No prefix defined for " + eventType));

        String body = String.format("%09d", RAND.nextInt(1_000_000_000)); // 000000000–999999999
        int checkDigit = luhnCheckDigit(body);

        return prefix + body + checkDigit;
    }

    /**
     * Computes the Luhn (mod-10) check digit for a numeric string.
     * <p>
     * Working right-to-left, every second digit is doubled; if the result
     * exceeds 9, 9 is subtracted. The adjusted digits are summed and the check
     * digit returned is the value that makes the total a multiple of 10.
     *
     * @param number the numeric body (digits only, without its check digit)
     * @return the check digit to append (0 – 9)
     * @throws IllegalArgumentException if {@code number} is {@code null} or contains non-digit characters
     */
    static int luhnCheckDigit(String number) {

        int sum = 0; // accumulates the adjusted digits as we iterate

        // is telling us whether the current digit (moving right-to-left) needs to be doubled
        boolean doubleDigit = true;

        // Iterate indices length-1 down to 0, rightmost to leftmost
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0'; // subtracting '0' converts the ASCII/UTF-16 code point to its integer value
            if (doubleDigit) {
                digit <<= 1; // left shift, same as digit *= 2
                if (digit > 9) digit -= 9;
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - sum % 10) % 10;

    }
}
