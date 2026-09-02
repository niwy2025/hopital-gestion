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
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hospital_medicine_stocks")
public class HospitalStockEntity {
    @Id private UUID id;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "medicine_id", nullable = false) private MedicineEntity medicine;
    @Column(nullable = false) private int quantity;
    @Column(name = "reorder_level", nullable = false) private int reorderLevel;
    @Column(name = "average_unit_cost", nullable = false, precision = 18, scale = 2) private BigDecimal averageUnitCost;
    @Column(name = "unit_selling_price", nullable = false, precision = 18, scale = 2) private BigDecimal unitSellingPrice;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 3) private Currency currency;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected HospitalStockEntity() { }

    public HospitalStockEntity(UUID id, UUID hospitalId, String hospitalCode, MedicineEntity medicine, int quantity,
            int reorderLevel, BigDecimal unitCost, BigDecimal unitSellingPrice, Currency currency, Instant updatedAt) {
        this.id = id; this.hospitalId = hospitalId; this.hospitalCode = hospitalCode; this.medicine = medicine;
        this.quantity = quantity; this.reorderLevel = reorderLevel; this.averageUnitCost = unitCost.setScale(2, RoundingMode.HALF_UP);
        this.unitSellingPrice = unitSellingPrice.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency; this.updatedAt = updatedAt;
    }

    public void receive(int incomingQuantity, BigDecimal unitCost, BigDecimal nextUnitSellingPrice, int nextReorderLevel, Instant receivedAt) {
        BigDecimal existingValue = averageUnitCost.multiply(BigDecimal.valueOf(quantity));
        BigDecimal incomingValue = unitCost.multiply(BigDecimal.valueOf(incomingQuantity));
        quantity += incomingQuantity;
        averageUnitCost = existingValue.add(incomingValue).divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
        unitSellingPrice = nextUnitSellingPrice.setScale(2, RoundingMode.HALF_UP);
        reorderLevel = nextReorderLevel;
        updatedAt = receivedAt;
    }

    public void issue(int outgoingQuantity, Instant occurredAt) {
        if (outgoingQuantity <= 0 || outgoingQuantity > quantity) {
            throw new IllegalArgumentException("La quantité à sortir dépasse le stock comptable.");
        }
        quantity -= outgoingQuantity;
        updatedAt = occurredAt;
    }

    public UUID getId() { return id; }
    public UUID getHospitalId() { return hospitalId; }
    public String getHospitalCode() { return hospitalCode; }
    public MedicineEntity getMedicine() { return medicine; }
    public int getQuantity() { return quantity; }
    public int getReorderLevel() { return reorderLevel; }
    public BigDecimal getAverageUnitCost() { return averageUnitCost; }
    public BigDecimal getUnitSellingPrice() { return unitSellingPrice; }
    public Currency getCurrency() { return currency; }
    public Instant getUpdatedAt() { return updatedAt; }
}
