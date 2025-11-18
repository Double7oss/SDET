package com.example.fxdeals.core;

import java.math.BigDecimal;

/** Minimal request model used by parser; fields are Strings for defensive parsing */
public class DealRequest {
    public String dealId;
    public String fromCurrency;
    public String toCurrency;
    public String timestamp;
    public BigDecimal amount;

    public DealRequest() {}
    public DealRequest(String dealId, String fromCurrency, String toCurrency, String timestamp, BigDecimal amount) {
        this.dealId = dealId;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.timestamp = timestamp;
        this.amount = amount;
    }
}
