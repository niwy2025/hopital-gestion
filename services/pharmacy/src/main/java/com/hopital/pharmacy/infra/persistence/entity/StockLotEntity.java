package com.hopital.pharmacy.infra.persistence.entity;

import com.hopital.pharmacy.application.domain.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "stock_lots")
public class StockLotEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 40) private String code;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "stock_id", nullable = false) private HospitalStockEntity stock;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "stock_entry_id", nullable = false) private StockEntryEntity stockEntry;
    @Column(name = "received_quantity", nullable = false) private int receivedQuantity;
    @Column(name = "remaining_quantity", nullable = false) private int remainingQuantity;
    @Column(name = "unit_cost", nullable = false, precision = 18, scale = 2) private BigDecimal unitCost;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 3) private Currency currency;
    @Column(name = "expires_on") private LocalDate expiresOn;
    @Column(name = "received_at", nullable = false) private Instant receivedAt;

    protected StockLotEntity() { }

    public StockLotEntity(UUID id, String code, HospitalStockEntity stock, StockEntryEntity stockEntry, int quantity,
            BigDecimal unitCost, Currency currency, LocalDate expiresOn, Instant receivedAt) {
        this.id = id; this.code = code; this.stock = stock; this.stockEntry = stockEntry; this.receivedQuantity = quantity;
        this.remainingQuantity = quantity; this.unitCost = unitCost; this.currency = currency; this.expiresOn = expiresOn; this.receivedAt = receivedAt;
    }

    public void consume(int quantity) {
        if (quantity <= 0 || quantity > remainingQuantity) throw new IllegalArgumentException("La quantité du lot est invalide.");
        remainingQuantity -= quantity;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public HospitalStockEntity getStock() { return stock; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public Currency getCurrency() { return currency; }
    public LocalDate getExpiresOn() { return expiresOn; }
    public Instant getReceivedAt() { return receivedAt; }
}
