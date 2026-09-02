package com.hopital.pharmacy.infra.persistence.entity;

import com.hopital.pharmacy.application.domain.AuditActor;
import com.hopital.pharmacy.application.domain.Currency;
import com.hopital.pharmacy.application.domain.StockMovementType;
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
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class StockMovementEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 30) private String code;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "stock_id", nullable = false) private HospitalStockEntity stock;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "stock_lot_id") private StockLotEntity stockLot;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "medicine_id", nullable = false) private MedicineEntity medicine;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private StockMovementType type;
    @Column(nullable = false) private int quantity;
    @Column(name = "unit_cost", nullable = false, precision = 18, scale = 2) private BigDecimal unitCost;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 3) private Currency currency;
    @Column(length = 2000) private String notes;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    @Column(name = "performed_by_user_id", nullable = false, length = 100) private String performedByUserId;
    @Column(name = "performed_by_username", nullable = false, length = 150) private String performedByUsername;

    protected StockMovementEntity() { }

    public StockMovementEntity(UUID id, String code, HospitalStockEntity stock, StockLotEntity stockLot, StockMovementType type,
            int quantity, BigDecimal unitCost, Currency currency, String notes, AuditActor actor, Instant occurredAt) {
        this.id = id; this.code = code; this.stock = stock; this.stockLot = stockLot; this.hospitalId = stock.getHospitalId();
        this.hospitalCode = stock.getHospitalCode(); this.medicine = stock.getMedicine(); this.type = type; this.quantity = quantity;
        this.unitCost = unitCost; this.currency = currency; this.notes = notes; this.occurredAt = occurredAt;
        this.performedByUserId = actor.userId(); this.performedByUsername = actor.username();
    }

    public UUID getId() { return id; } public String getCode() { return code; } public HospitalStockEntity getStock() { return stock; }
    public StockLotEntity getStockLot() { return stockLot; } public StockMovementType getType() { return type; } public int getQuantity() { return quantity; }
    public BigDecimal getUnitCost() { return unitCost; } public Currency getCurrency() { return currency; } public String getNotes() { return notes; }
    public Instant getOccurredAt() { return occurredAt; } public String getPerformedByUsername() { return performedByUsername; }
}
