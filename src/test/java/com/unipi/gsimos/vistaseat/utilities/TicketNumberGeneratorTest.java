package com.unipi.gsimos.vistaseat.utilities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TicketNumberGeneratorTest {

    @Test
    void luhnCheckDigit_examples() {
        // classic Wikipedia example
        assertEquals(4, TicketNumberGenerator.luhnCheckDigit("1789372997"));

        //
        assertEquals(7, TicketNumberGenerator.luhnCheckDigit("441029016844162"));
        assertEquals(7, TicketNumberGenerator.luhnCheckDigit("430252027579840"));

        // your “body” example
        assertEquals(7, TicketNumberGenerator.luhnCheckDigit("123456789"));
    }
}