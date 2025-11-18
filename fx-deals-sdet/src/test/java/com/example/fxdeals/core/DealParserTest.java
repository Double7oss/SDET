package com.example.fxdeals.core;

import com.example.fxdeals.domain.Deal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class DealParserTest {

    private final DealParser parser = new DealParser();
    private static final String VALID_ID = "11111111-1111-1111-1111-111111111111";
    private static final String VALID_TIMESTAMP = "2025-01-01T00:00:00Z";

    @Test
    void parsesHappyPath() {
        DealRequest req = new DealRequest(VALID_ID, "USD", "EUR", VALID_TIMESTAMP, new BigDecimal("10.00"));
        DealParser.ParseResult res = parser.parse(req);
        assertTrue(res.isOk());
        assertNotNull(res.deal);
    }

    @Test
    void rejectsNullRow() {
        DealParser.ParseResult res = parser.parse(null);
        assertFalse(res.isOk());
        assertEquals("INVALID_ROW", res.errorCode);
    }

    @Test
    void rejectsMissingFields() {
        DealParser.ParseResult res = parser
                .parse(new DealRequest(null, "USD", "EUR", VALID_TIMESTAMP, new BigDecimal("1")));
        assertFalse(res.isOk());
        assertEquals("MISSING_DEAL_ID", res.errorCode);
    }

    @Test
    void rejectsInvalidUuid() {
        DealParser.ParseResult res = parser
                .parse(new DealRequest("not-a-uuid", "USD", "EUR", VALID_TIMESTAMP, new BigDecimal("1")));
        assertFalse(res.isOk());
        assertEquals("INVALID_DEAL_ID", res.errorCode);
    }

    @Test
    void rejectsMissingFromCurrency() {
        DealParser.ParseResult res = parser
                .parse(new DealRequest(VALID_ID, null, "EUR", VALID_TIMESTAMP, new BigDecimal("1")));
        assertFalse(res.isOk());
        assertEquals("MISSING_FROM", res.errorCode);
    }

    @Test
    void rejectsMissingToCurrency() {
        DealParser.ParseResult res = parser
                .parse(new DealRequest(VALID_ID, "USD", null, VALID_TIMESTAMP, new BigDecimal("1")));
        assertFalse(res.isOk());
        assertEquals("MISSING_TO", res.errorCode);
    }

    @Test
    void rejectsInvalidCurrency() {
        DealParser.ParseResult res = parser
                .parse(new DealRequest(VALID_ID, "usd", "EUR", VALID_TIMESTAMP, new BigDecimal("1")));
        assertFalse(res.isOk());
        assertEquals("INVALID_FROM", res.errorCode);
    }

    @Test
    void rejectsInvalidToCurrency() {
        DealParser.ParseResult res = parser
                .parse(new DealRequest(VALID_ID, "USD", "eur", VALID_TIMESTAMP, new BigDecimal("1")));
        assertFalse(res.isOk());
        assertEquals("INVALID_TO", res.errorCode);
    }

    @Test
    void rejectsInvalidTimestamp() {
        DealParser.ParseResult res = parser
                .parse(new DealRequest(VALID_ID, "USD", "EUR", "not-time", new BigDecimal("1")));
        assertFalse(res.isOk());
        assertEquals("INVALID_TIMESTAMP", res.errorCode);
    }

    @Test
    void rejectsMissingTimestamp() {
        DealParser.ParseResult res = parser.parse(new DealRequest(VALID_ID, "USD", "EUR", null, new BigDecimal("1")));
        assertFalse(res.isOk());
        assertEquals("MISSING_TIMESTAMP", res.errorCode);
    }

    @Test
    void rejectsNonPositiveAmount() {
        DealParser.ParseResult res = parser
                .parse(new DealRequest(VALID_ID, "USD", "EUR", VALID_TIMESTAMP, new BigDecimal("-1")));
        assertFalse(res.isOk());
        assertEquals("INVALID_AMOUNT", res.errorCode);
    }

    @Test
    void rejectsMissingAmount() {
        DealParser.ParseResult res = parser.parse(new DealRequest(VALID_ID, "USD", "EUR", VALID_TIMESTAMP, null));
        assertFalse(res.isOk());
        assertEquals("MISSING_AMOUNT", res.errorCode);
    }

    @Test
    void rejectsBlankDealId() {
        DealParser.ParseResult res = parser
                .parse(new DealRequest("  ", "USD", "EUR", VALID_TIMESTAMP, new BigDecimal("1")));
        assertFalse(res.isOk());
        assertEquals("MISSING_DEAL_ID", res.errorCode);
    }

    @Test
    void rejectsBlankTimestamp() {
        DealParser.ParseResult res = parser.parse(new DealRequest(VALID_ID, "USD", "EUR", "  ", new BigDecimal("1")));
        assertFalse(res.isOk());
        assertEquals("MISSING_TIMESTAMP", res.errorCode);
    }

    @Test
    void rejectsInvalidCurrencyLength() {
        DealParser.ParseResult res = parser
                .parse(new DealRequest(VALID_ID, "US", "EUR", VALID_TIMESTAMP, new BigDecimal("1")));
        assertFalse(res.isOk());
        assertEquals("INVALID_FROM", res.errorCode);
    }
}
