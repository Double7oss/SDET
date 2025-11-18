package com.example.fxdeals.core;

import com.example.fxdeals.domain.Deal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

public class DealParser {

    public static class ParseResult {
        public final Deal deal;
        public final String errorCode;
        public final String errorMessage;

        private ParseResult(Deal deal, String errorCode, String errorMessage) {
            this.deal = deal;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public static ParseResult ok(Deal d) {
            return new ParseResult(d, null, null);
        }

        public static ParseResult error(String code, String msg) {
            return new ParseResult(null, code, msg);
        }

        public boolean isOk() {
            return deal != null;
        }
    }

    public ParseResult parse(DealRequest req) {
        if (req == null)
            return ParseResult.error("INVALID_ROW", "Row is null");
        // dealId
        if (req.dealId == null || req.dealId.isBlank())
            return ParseResult.error("MISSING_DEAL_ID", "dealId is required");
        UUID id;
        try {
            id = UUID.fromString(req.dealId.trim());
        } catch (IllegalArgumentException ex) {
            return ParseResult.error("INVALID_DEAL_ID", "dealId must be UUID");
        }

        // currencies
        String from = notNull(req.fromCurrency, "fromCurrency");
        if (from == null)
            return ParseResult.error("MISSING_FROM", "fromCurrency is required");
        String to = notNull(req.toCurrency, "toCurrency");
        if (to == null)
            return ParseResult.error("MISSING_TO", "toCurrency is required");
        if (!isIso3(from))
            return ParseResult.error("INVALID_FROM", "fromCurrency must be 3-letter ISO 4217 uppercase");
        if (!isIso3(to))
            return ParseResult.error("INVALID_TO", "toCurrency must be 3-letter ISO 4217 uppercase");

        // timestamp
        if (req.timestamp == null || req.timestamp.isBlank())
            return ParseResult.error("MISSING_TIMESTAMP", "timestamp is required (ISO-8601)");
        Instant ts;
        try {
            ts = Instant.parse(req.timestamp.trim());
        } catch (DateTimeParseException ex) {
            return ParseResult.error("INVALID_TIMESTAMP", "timestamp must be ISO-8601 instant");
        }

        // amount
        if (req.amount == null)
            return ParseResult.error("MISSING_AMOUNT", "amount is required");
        if (req.amount.compareTo(BigDecimal.ZERO) <= 0)
            return ParseResult.error("INVALID_AMOUNT", "amount must be positive");

        Deal deal = new Deal(id, from, to, ts, req.amount);
        return ParseResult.ok(deal);
    }

    private static boolean isIso3(String s) {
        // parse() guarantees non-null before invoking this helper
        return s.length() == 3 && s.toUpperCase(Locale.ROOT).equals(s);
    }

    private static String notNull(String s, String field) {
        return s;
    }
}
