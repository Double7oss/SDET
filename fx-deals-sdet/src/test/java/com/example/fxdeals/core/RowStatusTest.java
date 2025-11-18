package com.example.fxdeals.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RowStatusTest {

    @Test
    void verifiesEnumOrdering() {
        RowStatus[] values = RowStatus.values();
        assertEquals(RowStatus.ACCEPTED, values[0]);
        assertEquals(RowStatus.DUPLICATE, values[1]);
        assertEquals(RowStatus.REJECTED, values[2]);
    }
}
