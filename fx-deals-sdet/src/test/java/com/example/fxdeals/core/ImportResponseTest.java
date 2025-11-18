package com.example.fxdeals.core;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ImportResponseTest {

    @Test
    void builderProducesImmutableSnapshot() {
        ErrorRow error = new ErrorRow(1, "CODE", "message");
        ImportResponse response = ImportResponse.builder()
                .total(2)
                .accepted(1)
                .duplicates(0)
                .rejected(1)
                .addError(error)
                .build();

        assertEquals(2, response.getTotal());
        assertEquals(1, response.getAccepted());
        assertEquals(0, response.getDuplicates());
        assertEquals(1, response.getRejected());
        assertEquals(1, response.getErrors().size());
        assertSame(error, response.getErrors().get(0));
        assertEquals(1, response.getErrors().get(0).getIndex());
        assertEquals("CODE", response.getErrors().get(0).getCode());
        assertEquals("message", response.getErrors().get(0).getMessage());
    }

    @Test
    void dealRequestDefaultConstructorIsMutable() {
        DealRequest request = new DealRequest();
        request.dealId = "c1e504e0-2b10-4b83-9ca0-caba9efba001";
        request.fromCurrency = "USD";
        request.toCurrency = "EUR";
        request.timestamp = "2025-01-01T00:00:00Z";
        request.amount = new BigDecimal("7.77");

        assertEquals("c1e504e0-2b10-4b83-9ca0-caba9efba001", request.dealId);
        assertEquals("USD", request.fromCurrency);
        assertEquals("EUR", request.toCurrency);
        assertEquals("2025-01-01T00:00:00Z", request.timestamp);
        assertEquals(new BigDecimal("7.77"), request.amount);
    }
}
