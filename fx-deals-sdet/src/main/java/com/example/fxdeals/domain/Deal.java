package com.example.fxdeals.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deals")
public class Deal {

    @Id
    @Column(name = "deal_id", nullable = false, updatable = false)
    private UUID dealId;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @Column(name = "deal_timestamp", nullable = false)
    private Instant dealTimestamp;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    protected Deal() {}

    public Deal(UUID dealId, String fromCurrency, String toCurrency, Instant dealTimestamp, BigDecimal amount) {
        this.dealId = dealId;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.dealTimestamp = dealTimestamp;
        this.amount = amount;
    }

    public UUID getDealId() { return dealId; }
    public String getFromCurrency() { return fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public Instant getDealTimestamp() { return dealTimestamp; }
    public BigDecimal getAmount() { return amount; }
}
