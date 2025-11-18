package com.example.fxdeals.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorRowTest {

    @Test
    void exposesGetters() {
        ErrorRow row = new ErrorRow(5, "INVALID", "broken");
        assertEquals(5, row.getIndex());
        assertEquals("INVALID", row.getCode());
        assertEquals("broken", row.getMessage());
    }
}
